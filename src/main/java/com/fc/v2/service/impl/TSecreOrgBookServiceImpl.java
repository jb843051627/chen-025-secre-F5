package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TSecreOrgBookMapper;
import com.fc.v2.mapper.auto.TSecreOrgSyncConflictMapper;
import com.fc.v2.model.auto.TSecreOrgBook;
import com.fc.v2.model.auto.TSecreOrgSyncConflict;
import com.fc.v2.service.ITSecreOrgBookService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构名录底册 Service业务层处理
 *
 * 名录可能一头一份（sync_kind 区分上级下发／本机关自录）。同代号两份打架时：
 * 以本机关自录那份为准（全称、在册情形都听本地的，上级说已撤号也不动本地），
 * 但绝不悄悄盖掉——逐条点代号落冲突台账，并把缘由回写本地名录备注。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TSecreOrgBookServiceImpl implements ITSecreOrgBookService {

    private static final int REMARK_MAX = 500;

    @Resource
    private TSecreOrgBookMapper orgBookMapper;

    @Resource
    private TSecreOrgSyncConflictMapper conflictMapper;

    @Override
    public TSecreOrgBook selectByBookNo(String bookNo) {
        if (StringUtils.isEmpty(bookNo)) {
            return null;
        }
        List<TSecreOrgBook> rows = orgBookMapper.selectList(new QueryWrapper<TSecreOrgBook>()
                .eq("book_no", bookNo)
                .eq("del_flag", 0)
                .orderByAsc("sync_kind"));
        return effective(rows);
    }

    @Override
    public List<TSecreOrgBook> listAll() {
        List<TSecreOrgBook> rows = orgBookMapper.selectList(new QueryWrapper<TSecreOrgBook>()
                .eq("del_flag", 0)
                .orderByAsc("book_no"));
        // 同代号以本机关自录为准，只回一条作准的
        Map<String, TSecreOrgBook> byNo = new LinkedHashMap<String, TSecreOrgBook>();
        Map<String, List<TSecreOrgBook>> grouped = new LinkedHashMap<String, List<TSecreOrgBook>>();
        for (TSecreOrgBook r : rows) {
            List<TSecreOrgBook> list = grouped.get(r.getBookNo());
            if (list == null) {
                list = new ArrayList<TSecreOrgBook>();
                grouped.put(r.getBookNo(), list);
            }
            list.add(r);
        }
        for (Map.Entry<String, List<TSecreOrgBook>> e : grouped.entrySet()) {
            byNo.put(e.getKey(), effective(e.getValue()));
        }
        return new ArrayList<TSecreOrgBook>(byNo.values());
    }

    @Override
    public int saveLocal(TSecreOrgBook book) {
        if (book == null || StringUtils.isEmpty(book.getBookNo())) {
            return 0;
        }
        book.setSyncKind(SYNC_KIND_LOCAL);
        if (book.getStatus() == null) {
            book.setStatus(BOOK_ACTIVE);
        }
        if (book.getDelFlag() == null) {
            book.setDelFlag(0);
        }
        TSecreOrgBook exist = findBySync(book.getBookNo(), SYNC_KIND_LOCAL);
        if (exist == null) {
            return orgBookMapper.insert(book);
        }
        book.setId(exist.getId());
        return orgBookMapper.updateById(book);
    }

    @Override
    public List<TSecreOrgSyncConflict> reconcileFromUpper(List<TSecreOrgBook> upperRows) {
        List<TSecreOrgSyncConflict> conflicts = new ArrayList<TSecreOrgSyncConflict>();
        if (upperRows == null || upperRows.isEmpty()) {
            return conflicts;
        }
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        for (TSecreOrgBook upper : upperRows) {
            if (upper == null || StringUtils.isEmpty(upper.getBookNo())) {
                continue;
            }
            upper.setSyncKind(SYNC_KIND_UPPER);
            if (upper.getStatus() == null) {
                upper.setStatus(BOOK_ACTIVE);
            }

            // 上级那份也照实落一份（两份都留着，谁也不被悄悄盖掉）
            TSecreOrgBook upperExist = findBySync(upper.getBookNo(), SYNC_KIND_UPPER);
            if (upperExist == null) {
                upper.setDelFlag(0);
                orgBookMapper.insert(upper);
            } else {
                upper.setId(upperExist.getId());
                orgBookMapper.updateById(upper);
            }

            TSecreOrgBook local = findBySync(upper.getBookNo(), SYNC_KIND_LOCAL);
            if (local == null) {
                // 本地没有自录的，就不存在「打架」，上级那份直接在册
                continue;
            }

            boolean nameDiff = !eq(upper.getBookName(), local.getBookName());
            int upperStatus = upper.getStatus() == null ? BOOK_ACTIVE : upper.getStatus();
            int localStatus = local.getStatus() == null ? BOOK_ACTIVE : local.getStatus();
            boolean statusDiff = upperStatus != localStatus;
            if (!nameDiff && !statusDiff) {
                continue;
            }

            // 同代号两份对不上：以本机关自录为准——全称、在册情形一律不动本地
            String reason = String.format(
                    "【上级对账冲突 %s 代号%s】上级：全称「%s」、%s；本地：全称「%s」、%s。按今年口径以本机关自录那份为准，本地不动。",
                    dayFmt.format(new Date()), upper.getBookNo(),
                    nz(upper.getBookName()), upperStatus == BOOK_WITHDRAWN ? "已撤号" : "在册",
                    nz(local.getBookName()), localStatus == BOOK_WITHDRAWN ? "已撤出" : "在册");

            TSecreOrgSyncConflict c = new TSecreOrgSyncConflict();
            c.setBookNo(upper.getBookNo());
            c.setUpperBookName(upper.getBookName());
            c.setLocalBookName(local.getBookName());
            c.setUpperStatus(upperStatus);
            c.setLocalStatus(localStatus);
            c.setReason(reason);
            c.setDelFlag(0);
            conflictMapper.insert(c);
            conflicts.add(c);

            // 缘由一并落到本地这份名录的备注里，本地这份名录才算交代过
            String merged = appendRemark(local.getRemark(), reason);
            TSecreOrgBook note = new TSecreOrgBook();
            note.setId(local.getId());
            note.setRemark(merged);
            orgBookMapper.updateById(note);
        }
        return conflicts;
    }

    @Override
    public List<TSecreOrgSyncConflict> listConflicts() {
        return conflictMapper.selectList(new QueryWrapper<TSecreOrgSyncConflict>()
                .eq("del_flag", 0)
                .orderByAsc("book_no")
                .orderByDesc("create_time"));
    }

    /** 同代号两份里作准的那一条：有本机关自录听本地的，没有才听上级的 */
    private TSecreOrgBook effective(List<TSecreOrgBook> sameNo) {
        if (sameNo == null || sameNo.isEmpty()) {
            return null;
        }
        TSecreOrgBook upper = null;
        for (TSecreOrgBook r : sameNo) {
            if (SYNC_KIND_LOCAL.equals(r.getSyncKind())) {
                return r;
            }
            if (SYNC_KIND_UPPER.equals(r.getSyncKind())) {
                upper = r;
            }
        }
        return upper != null ? upper : sameNo.get(0);
    }

    private TSecreOrgBook findBySync(String bookNo, String syncKind) {
        List<TSecreOrgBook> rows = orgBookMapper.selectList(new QueryWrapper<TSecreOrgBook>()
                .eq("book_no", bookNo)
                .eq("sync_kind", syncKind)
                .eq("del_flag", 0)
                .last("limit 1"));
        return rows.isEmpty() ? null : rows.get(0);
    }

    private boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private String appendRemark(String oldRemark, String reason) {
        String merged = StringUtils.isEmpty(oldRemark) ? reason : oldRemark + " | " + reason;
        return merged.length() > REMARK_MAX ? merged.substring(0, REMARK_MAX) : merged;
    }
}
