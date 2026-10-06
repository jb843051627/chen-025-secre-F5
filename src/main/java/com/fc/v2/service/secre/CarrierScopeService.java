package com.fc.v2.service.secre;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TSecreScopeBookMapper;
import com.fc.v2.mapper.auto.TSecreUserScopeMapper;
import com.fc.v2.model.auto.TSecreOrgBook;
import com.fc.v2.model.auto.TSecreScopeBook;
import com.fc.v2.model.auto.TSecreUserScope;
import com.fc.v2.model.auto.TSysUser;
import com.fc.v2.service.ITSecreOrgBookService;
import com.fc.v2.shiro.util.ShiroUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 知悉范围（这一屏该见哪几行）的**唯一一处判路**。
 *
 * 口径：
 * - 本级经办岗（scope_type=0）：只见挂在本级属地（road_name 一直写到街道）名录名下的行；
 *   名录撤出只挡新行、不抹旧账，所以已撤出名录若属本地，旧行仍在视野内。
 * - 市保密办（scope_type=1）：三层都看得见，但只看得见点名划给它的那几家（t_secre_scope_book）。
 * - 没划知悉范围、或这一季没有行的，一屏就是空的——绝不从邻家借行撑场面。
 *
 * 列表、逐行回查、入册挂名录三处一律走这里，旁处不许留第二处判路。
 *
 * @author 市保密办规矩落地
 */
@Service
public class CarrierScopeService {

    /** 屏别：本级经办岗 */
    public static final int SCOPE_TYPE_LOCAL = 0;
    /** 屏别：市保密办 */
    public static final int SCOPE_TYPE_CITY = 1;

    @Resource
    private TSecreUserScopeMapper userScopeMapper;

    @Resource
    private TSecreScopeBookMapper scopeBookMapper;

    @Resource
    private ITSecreOrgBookService orgBookService;

    /** 一屏的视野：看得见的名录代号集合（行一律按名录代号文本挂） */
    public static final class View {
        private final Long userId;
        private final int scopeType;
        private final String roadName;
        private final Set<String> bookNos;

        private View(Long userId, int scopeType, String roadName, Set<String> bookNos) {
            this.userId = userId;
            this.scopeType = scopeType;
            this.roadName = roadName;
            this.bookNos = bookNos;
        }

        public Long getUserId() {
            return userId;
        }

        public int getScopeType() {
            return scopeType;
        }

        public String getRoadName() {
            return roadName;
        }

        public Set<String> getBookNos() {
            return bookNos;
        }

        /** 这一屏能不能见挂在该代号名下的行 */
        public boolean canSee(String bookNo) {
            return bookNo != null && bookNos.contains(bookNo);
        }
    }

    /** 没有划入任何知悉范围的空视野 */
    private View emptyView(Long userId) {
        return new View(userId, SCOPE_TYPE_LOCAL, null, Collections.<String>emptySet());
    }

    /**
     * 取当前登录人这一屏的视野。判定只此一处：
     * 配置→屏别→（市办：点名名录 / 本级：属地到街道的名录，含已撤出但旧账仍在的）。
     */
    public View currentView() {
        TSysUser user;
        try {
            user = ShiroUtils.getUser();
        } catch (RuntimeException e) {
            return emptyView(null);
        }
        if (user == null || user.getId() == null) {
            return emptyView(null);
        }

        List<TSecreUserScope> cfgs = userScopeMapper.selectList(new QueryWrapper<TSecreUserScope>()
                .eq("user_id", user.getId())
                .eq("del_flag", 0)
                .orderByAsc("id")
                .last("limit 1"));
        if (cfgs == null || cfgs.isEmpty()) {
            return emptyView(user.getId());
        }
        TSecreUserScope cfg = cfgs.get(0);

        if (cfg.getScopeType() != null && cfg.getScopeType() == SCOPE_TYPE_CITY) {
            // 市保密办：只看见点名划给它的那几家（不论在册还是已撤出——撤出只挡新行不抹旧账）
            List<TSecreScopeBook> assigned = scopeBookMapper.selectList(new QueryWrapper<TSecreScopeBook>()
                    .eq("scope_id", cfg.getId())
                    .eq("del_flag", 0));
            Set<String> bookNos = new LinkedHashSet<String>();
            for (TSecreScopeBook b : assigned) {
                if (b.getBookNo() != null && !b.getBookNo().isEmpty()) {
                    bookNos.add(b.getBookNo());
                }
            }
            return new View(user.getId(), SCOPE_TYPE_CITY, null, bookNos);
        }

        // 本级经办岗：属地一直写到街道，按属地把作准名录（含已撤出的）整串捞出来
        Set<String> bookNos = new LinkedHashSet<String>();
        if (cfg.getRoadName() != null && !cfg.getRoadName().isEmpty()) {
            // 同代号以本机关自录那份作准（路名也听本地的），撤出只挡新行不抹旧账，故不剔除已撤出
            for (TSecreOrgBook b : orgBookService.listAll()) {
                if (cfg.getRoadName().equals(b.getRoadName())
                        && b.getBookNo() != null && !b.getBookNo().isEmpty()) {
                    bookNos.add(b.getBookNo());
                }
            }
            return new View(user.getId(), SCOPE_TYPE_LOCAL, cfg.getRoadName(), bookNos);
        }
        return new View(user.getId(), SCOPE_TYPE_LOCAL, cfg.getRoadName(), bookNos);
    }

    /**
     * 把视野套到查询上（唯一一处拼接视野条件）。
     * 视野为空时强制 1=0：宁可空屏，不许漏出别家一行。
     */
    public QueryWrapper<com.fc.v2.model.auto.TSecreCarrier> apply(
            QueryWrapper<com.fc.v2.model.auto.TSecreCarrier> wrapper, View view) {
        if (view == null || view.getBookNos().isEmpty()) {
            wrapper.apply("1 = 0");
            return wrapper;
        }
        wrapper.in("book_no", new ArrayList<String>(view.getBookNos()));
        return wrapper;
    }

    /** 当前登录人能否见该代号名下的行（逐行回查/入册挂接共用） */
    public boolean canSee(String bookNo) {
        return currentView().canSee(bookNo);
    }
}
