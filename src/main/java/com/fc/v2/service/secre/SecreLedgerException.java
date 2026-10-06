package com.fc.v2.service.secre;

/**
 * 入册立账被打回时抛出：消息必须点到具体栏位与毛病，不许只丢一句「数不对劲」。
 *
 * @author 市保密办规矩落地
 */
public class SecreLedgerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 出毛病的栏位（册面字段名） */
    private final String field;

    public SecreLedgerException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
