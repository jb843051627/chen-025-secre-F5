package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TSecreCarrierMapper;
import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreOrgBook;
import com.fc.v2.service.ITSecreCarrierService;
import com.fc.v2.service.ITSecreOrgBookService;
import com.fc.v2.service.secre.CarrierLedger;
import com.fc.v2.service.secre.CarrierScopeService;
import com.fc.v2.service.secre.SecreLedgerException;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 涉密载体册面Service业务层处理
 *
 * 全系统入册、改口、读数只认这一处 + {@link CarrierLedger} + {@link CarrierScopeService}：
 * 差额当场轧、栏位逐个点、行只认名录代号、撤出只挡新行、视野只认划给本屏的那几家。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TSecreCarrierServiceImpl extends ServiceImpl<TSecreCarrierMapper, TSecreCarrier> implements ITSecreCarrierService {

    /** 名录情形：已撤出 */
    private static final int BOOK_WITHDRAWN = 1;
    /** 册面情形：已收齐 */
    private static final int ROW_COMPLETE = 1;
    /** 册面情形：缺项 */
    private static final int ROW_LACK = 2;

    @Autowired
    private ITSecreOrgBookService secreOrgBookService;

    @Autowired
    private CarrierScopeService carrierScopeService;

    @Override
    public TSecreCarrier selectTSecreCarrierById(Long id) {
        if (id == null) {
            return null;
        }
        TSecreCarrier row = this.baseMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                .eq("id", id)
                .eq("del_flag", 0));
        if (row == null) {
            return null;
        }
        // 库里入过的每一行本屏也都寻得着——但别家的行混不进来
        if (!carrierScopeService.currentView().canSee(row.getBookNo())) {
            return null;
        }
        recount(row);
        return row;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TSecreCarrier> selectTSecreCarrierList(Wrapper<TSecreCarrier> queryWrapper) {
        QueryWrapper<TSecreCarrier> wrapper = queryWrapper instanceof QueryWrapper
                ? (QueryWrapper<TSecreCarrier>) queryWrapper
                : new QueryWrapper<TSecreCarrier>();
        wrapper.eq("del_flag", 0);
        carrierScopeService.apply(wrapper, carrierScopeService.currentView());
        List<TSecreCarrier> rows = this.baseMapper.selectList(wrapper);
        for (TSecreCarrier row : rows) {
            recount(row);
        }
        return rows;
    }

    @Override
    public List<TSecreCarrier> listForScreen(TSecreCarrier query, String quarter) {
        QueryWrapper<TSecreCarrier> wrapper = new QueryWrapper<TSecreCarrier>();
        wrapper.eq("del_flag", 0);
        if (query != null) {
            // 件码精确回查不按季截断：库里入过的每一行，这一屏都寻得着
            wrapper.eq(StringUtils.isNotEmpty(query.getCarrierNo()), "carrier_no", query.getCarrierNo());
            wrapper.eq(StringUtils.isNotEmpty(query.getBookNo()), "book_no", query.getBookNo());
            wrapper.eq(StringUtils.isNotEmpty(query.getPostNo()), "post_no", query.getPostNo());
            wrapper.eq(query.getStatus() != null, "status", query.getStatus());
        }
        boolean exactByCode = query != null && StringUtils.isNotEmpty(query.getCarrierNo());
        if (!exactByCode && !"all".equalsIgnoreCase(quarter == null ? "" : quarter)) {
            String[] span = quarterSpan(quarter);
            wrapper.ge("create_time", span[0]).lt("create_time", span[1]);
        }
        wrapper.orderByDesc("create_time");
        carrierScopeService.apply(wrapper, carrierScopeService.currentView());
        List<TSecreCarrier> rows = this.baseMapper.selectList(wrapper);
        for (TSecreCarrier row : rows) {
            recount(row);
        }
        return rows;
    }

    @Override
    public int insertTSecreCarrier(TSecreCarrier record) {
        if (record == null) {
            return 0;
        }

        // 挂名录只认文本代号；报文里塞 book_id 主键不作数
        record.setBookId(null);
        // 差额报文带多少都不作数，先抹掉，落库前由本行自己的两个数重轧
        record.setLackNum(null);

        TSecreOrgBook book = resolveBook(record.getBookNo());
        if (book.getStatus() != null && book.getStatus() == BOOK_WITHDRAWN) {
            // 名录已撤出（单位并走/改隶属）：只挡新行，不抹它名下的旧账
            throw new SecreLedgerException(CarrierLedger.FIELD_BOOK_NO,
                    "名录代号「" + book.getBookNo() + "」已撤出，不能再往它名下入新行（撤出前的旧账照旧保留）");
        }
        if (!carrierScopeService.currentView().canSee(book.getBookNo())) {
            throw new SecreLedgerException(CarrierLedger.FIELD_BOOK_NO,
                    "名录代号「" + book.getBookNo() + "」不在本屏知悉范围内，不能往它名下入新行");
        }

        if (StringUtils.isNotEmpty(record.getCarrierNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TSecreCarrier>()
                    .eq("carrier_no", record.getCarrierNo()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                throw new SecreLedgerException("carrierNo",
                        "涉密载体件码（carrierNo）「" + record.getCarrierNo() + "」已入过册，不能重复入册");
            }
        }

        settle(record);
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTSecreCarrier(TSecreCarrier record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        TSecreCarrier old = this.baseMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
        if (old == null || !carrierScopeService.currentView().canSee(old.getBookNo())) {
            // 查无此行或不在本屏：按立不起来处理，不许借改口挪别家的行
            throw new SecreLedgerException("id", "这一行在本屏册面上寻不着，改不了口");
        }

        // 挂名录只认文本代号：没改口就沿用旧代号；改了口必须对得上在册名录、且在本屏
        String targetBookNo = StringUtils.isNotEmpty(record.getBookNo()) ? record.getBookNo() : old.getBookNo();
        TSecreOrgBook target = resolveBook(targetBookNo);
        boolean moving = !targetBookNo.equals(old.getBookNo());
        if (moving && target.getStatus() != null && target.getStatus() == BOOK_WITHDRAWN) {
            throw new SecreLedgerException(CarrierLedger.FIELD_BOOK_NO,
                    "名录代号「" + target.getBookNo() + "」已撤出，不能再往它名下挂行（撤出只挡新行）");
        }
        if (!carrierScopeService.currentView().canSee(targetBookNo)) {
            throw new SecreLedgerException(CarrierLedger.FIELD_BOOK_NO,
                    "名录代号「" + targetBookNo + "」不在本屏知悉范围内，不能把行挂过去");
        }
        record.setBookNo(targetBookNo);
        record.setBookId(null);

        if (StringUtils.isNotEmpty(record.getCarrierNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TSecreCarrier>()
                    .eq("carrier_no", record.getCarrierNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                throw new SecreLedgerException("carrierNo",
                        "涉密载体件码（carrierNo）「" + record.getCarrierNo() + "」已被别行占用");
            }
        }

        // 改口只动报来的栏，没报来的沿用旧值，然后一律重轧
        if (record.getNeedNum() == null) {
            record.setNeedNum(old.getNeedNum());
        }
        if (record.getGotNum() == null) {
            record.setGotNum(old.getGotNum());
        }
        settle(record);

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new QueryWrapper<TSecreCarrier>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTSecreCarrierByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        List<Long> allowed = new ArrayList<Long>();
        for (Long id : idArr) {
            TSecreCarrier row = this.baseMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                    .eq("id", id).eq("del_flag", 0));
            if (row != null && carrierScopeService.currentView().canSee(row.getBookNo())) {
                allowed.add(id);
            }
        }
        if (allowed.isEmpty()) {
            return 0;
        }
        return this.baseMapper.deleteBatchIds(allowed);
    }

    @Override
    public int deleteTSecreCarrierById(Long id) {
        TSecreCarrier row = this.baseMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                .eq("id", id).eq("del_flag", 0));
        if (row == null || !carrierScopeService.currentView().canSee(row.getBookNo())) {
            return 0;
        }
        return this.baseMapper.deleteById(id);
    }

    /**
     * 按名录代号文本对册面：对不上（含空代号、名录不存在、已删除）按栏位点名打回。
     * 同代号有上级/自录两份时，取以本机关自录为准的作准那一条（ITSecreOrgBookService 唯一判路）。
     * 绝不用名录主键挂行——主键是一长串数字，文本代号才挂得稳。
     */
    private TSecreOrgBook resolveBook(String bookNo) {
        if (StringUtils.isEmpty(bookNo)) {
            CarrierLedger.requireBookMatched(null, false);
        }
        TSecreOrgBook book = secreOrgBookService.selectByBookNo(bookNo);
        CarrierLedger.requireBookMatched(bookNo, book != null);
        return book;
    }

    /**
     * 落库前的唯一一处收口：栏位逐个校验 + 差额当场重轧 + 册面情形跟着差额走。
     * 新增、改已收、换名录三道都从这里过，报文里的 lackNum 永远进不了库。
     */
    private void settle(TSecreCarrier record) {
        int lack = CarrierLedger.validate(record.getNeedNum(), record.getGotNum());
        if (record.getGotNum() == null) {
            record.setGotNum(0);
        }
        record.setLackNum(lack);
        record.setStatus(lack == 0 ? ROW_COMPLETE : ROW_LACK);
    }

    /**
     * 读数这一遍也走同一处账：页面摆出来的差额 = 回库逐行点出来的差额 = CarrierLedger.lack。
     */
    private void recount(TSecreCarrier row) {
        if (row == null || row.getNeedNum() == null) {
            return;
        }
        int got = row.getGotNum() == null ? 0 : row.getGotNum();
        row.setLackNum(CarrierLedger.lack(row.getNeedNum(), got));
    }

    /**
     * 季度串（yyyyQX，如 2026Q3；null/空当季；非法串抛栏位级错误）转 [起, 止) 墙钟串。
     */
    private String[] quarterSpan(String quarter) {
        String q = quarter;
        if (StringUtils.isEmpty(q)) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int qi = c.get(java.util.Calendar.MONTH) / 3 + 1;
            return span(year, qi);
        }
        q = q.trim().toUpperCase();
        if (!q.matches("\\d{4}Q[1-4]")) {
            throw new SecreLedgerException("quarter",
                    "季度（quarter）格式「" + quarter + "」不对，应形如 2026Q3");
        }
        return span(Integer.parseInt(q.substring(0, 4)), Integer.parseInt(q.substring(5, 6)));
    }

    private String[] span(int year, int quarterIndex) {
        int startMonth = (quarterIndex - 1) * 3 + 1;
        int endYear = startMonth == 10 ? year + 1 : year;
        int endMonth = startMonth == 10 ? 1 : startMonth + 3;
        return new String[] {
                String.format("%04d-%02d-01 00:00:00", year, startMonth),
                String.format("%04d-%02d-01 00:00:00", endYear, endMonth)
        };
    }
}
