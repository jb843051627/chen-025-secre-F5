package com.fc.v2.service.secre;

/**
 * 涉密载体入册规矩的**唯一一处判路**。
 *
 * 规矩（市保密办今年口径，逐句落到此）：
 * 1. 「还差几件」这一格不由人手填、不由报文说了算，只能由本行自己的两个数当场轧出：
 *    差额 = 应交 - 已收。新增入册、改已收件数、换挂靠名录，落库前一律在这一处重轧。
 * 2. 立不立得起来也只在这一处判：应交空着/负数/小于已收、已收冒过应交，按栏位点名打回；
 *    名录代号对不上册面，由调用方查名录后经 {@link #requireBookMatched} 点名打回。
 * 3. 页面摆出来的数与回库逐行点出来的数，同走 {@link #lack}，不许留第二处判路。
 *
 * 纯函数、无状态，入库、改口、读数、回库重盘四处都只能调这里。
 *
 * @author 市保密办规矩落地
 */
public final class CarrierLedger {

    /** 册面栏位名（打回时点名用，与 t_secre_carrier 列名一一对齐） */
    public static final String FIELD_NEED_NUM = "needNum";
    public static final String FIELD_GOT_NUM = "gotNum";
    public static final String FIELD_BOOK_NO = "bookNo";

    private CarrierLedger() {
    }

    /**
     * 当场轧差额：应交减已收。全系统**唯一**的差额算法。
     * 应交、已收都假定已过 {@link #validate}（非空且不越界）。
     */
    public static int lack(int needNum, int gotNum) {
        return needNum - gotNum;
    }

    /**
     * 立账校验：应交、已收两栏逐栏点名。
     *
     * @return 本行轧出的差额（应交 - 已收），永不为负
     * @throws SecreLedgerException 应交空着/负数/小于已收，或已收为负/冒过应交时按栏位抛出
     */
    public static int validate(Integer needNum, Integer gotNum) {
        if (needNum == null) {
            throw new SecreLedgerException(FIELD_NEED_NUM, "应交件数（needNum）空着，这一行立不起来");
        }
        if (needNum < 0) {
            throw new SecreLedgerException(FIELD_NEED_NUM,
                    "应交件数（needNum）写成负数 " + needNum + "，这一行立不起来");
        }
        int got = gotNum == null ? 0 : gotNum;
        if (got < 0) {
            throw new SecreLedgerException(FIELD_GOT_NUM,
                    "已收件数（gotNum）写成负数 " + got + "，这一行立不起来");
        }
        if (got > needNum) {
            // 既可以报「已收冒过应交」，也等价于「应交小于已收」，点在已收栏最直白
            throw new SecreLedgerException(FIELD_GOT_NUM,
                    "已收件数（gotNum）" + got + " 冒过了应交件数（needNum）" + needNum + "，这一行立不起来");
        }
        return lack(needNum, got);
    }

    /**
     * 名录代号必须与册面对得上（在册名录里查得到，且不允许是已撤出的名录——撤出只挡新行）。
     * 行与名录的关联一律走名录代号文本，不走名录主键。
     *
     * @param bookMatched 代号是否在册面（在册、未删除）中查得到
     */
    public static void requireBookMatched(String bookNo, boolean bookMatched) {
        if (!bookMatched) {
            throw new SecreLedgerException(FIELD_BOOK_NO,
                    "名录代号（bookNo）" + (bookNo == null ? "空着" : "「" + bookNo + "」")
                            + "与册面对不上，这一行立不起来");
        }
    }
}
