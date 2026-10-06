package com.fc.v2.service.secre;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 「还差几件」唯一判路 CarrierLedger 的规矩用例：
 * 应交三十配已收十二落十八；应交二十配已收七落十三；两数齐平落零；
 * 应交空着/负数/小于已收、已收冒过应交，按栏位点名打回。
 */
public class CarrierLedgerTest {

    @Test
    public void lackIsNeedMinusGot() {
        assertEquals(18, CarrierLedger.lack(30, 12));
        assertEquals(13, CarrierLedger.lack(20, 7));
        assertEquals(0, CarrierLedger.lack(0, 0));
    }

    @Test
    public void validateReturnsRecountedLack() {
        assertEquals(18, CarrierLedger.validate(30, 12));
        assertEquals(13, CarrierLedger.validate(20, 7));
        // 已收空着按 0：应交多少差多少
        assertEquals(5, CarrierLedger.validate(5, null));
        // 齐平落零
        assertEquals(0, CarrierLedger.validate(9, 9));
    }

    @Test
    public void nullNeedRejectedOnNeedColumn() {
        SecreLedgerException e = assertThrows(SecreLedgerException.class,
                () -> CarrierLedger.validate(null, 3));
        assertEquals(CarrierLedger.FIELD_NEED_NUM, e.getField());
        assertTrue(e.getMessage().contains("应交"));
    }

    @Test
    public void negativeNeedRejectedOnNeedColumn() {
        SecreLedgerException e = assertThrows(SecreLedgerException.class,
                () -> CarrierLedger.validate(-1, 0));
        assertEquals(CarrierLedger.FIELD_NEED_NUM, e.getField());
        assertTrue(e.getMessage().contains("负数"));
    }

    @Test
    public void gotOverNeedRejectedOnGotColumn() {
        // 应交小于已收（已收冒过应交）：点在已收栏
        SecreLedgerException e = assertThrows(SecreLedgerException.class,
                () -> CarrierLedger.validate(5, 6));
        assertEquals(CarrierLedger.FIELD_GOT_NUM, e.getField());
        assertTrue(e.getMessage().contains("冒过"));
    }

    @Test
    public void negativeGotRejectedOnGotColumn() {
        SecreLedgerException e = assertThrows(SecreLedgerException.class,
                () -> CarrierLedger.validate(5, -2));
        assertEquals(CarrierLedger.FIELD_GOT_NUM, e.getField());
    }

    @Test
    public void bookMismatchRejectedOnBookNoColumn() {
        SecreLedgerException e = assertThrows(SecreLedgerException.class,
                () -> CarrierLedger.requireBookMatched("ML404", false));
        assertEquals(CarrierLedger.FIELD_BOOK_NO, e.getField());
        assertTrue(e.getMessage().contains("ML404"));
    }

    @Test
    public void bookMatchedPasses() {
        CarrierLedger.requireBookMatched("ML00", true);
    }
}
