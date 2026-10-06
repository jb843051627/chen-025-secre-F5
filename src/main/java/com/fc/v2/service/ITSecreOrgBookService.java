package com.fc.v2.service;

import com.fc.v2.model.auto.TSecreOrgBook;
import com.fc.v2.model.auto.TSecreOrgSyncConflict;

import java.util.List;

/**
 * 机构名录底册 Service接口
 *
 * 口径：行挂名录只走 book_no 文本；名录撤出只挡新行不抹旧账；
 * 上级下发与本机关自录同代号打架时**以本机关自录为准**，冲突逐条点代号报出、缘由落本地名录备注。
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITSecreOrgBookService {

    /** 名录来路：上级名录下发 */
    String SYNC_KIND_UPPER = "上级名录下发";
    /** 名录来路：本机关自录 */
    String SYNC_KIND_LOCAL = "本机关自录";

    /** 名录情形：在册 */
    int BOOK_ACTIVE = 0;
    /** 名录情形：已撤出／上级已撤号 */
    int BOOK_WITHDRAWN = 1;

    /** 按代号查在册面（含已撤出，未删除） */
    TSecreOrgBook selectByBookNo(String bookNo);

    /** 列全部在册面的名录（含已撤出——撤出不抹名录） */
    List<TSecreOrgBook> listAll();

    /** 本机关自录一条（来路盖成本机关自录） */
    int saveLocal(TSecreOrgBook book);

    /**
     * 每日上级名录下发对账：
     * - 上级有、本地无：按上级那份落库（来路记上级下发）；
     * - 同代号两头都有且全称/在册情形对不上：以本机关自录那份为准，本地不动，
     *   逐条点代号记入冲突台账，并把缘由回写到本地名录备注（不悄悄盖掉任何一份）。
     * 返回本次逐条点名的冲突清单（按代号升序）。
     */
    List<TSecreOrgSyncConflict> reconcileFromUpper(List<TSecreOrgBook> upperRows);

    /** 当前未销的冲突台账（逐条点代号） */
    List<TSecreOrgSyncConflict> listConflicts();
}
