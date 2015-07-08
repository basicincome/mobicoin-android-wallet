package de.schildbach.wallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by yezune on 15. 7. 8..
 */
public class UnpayValue {

    public static final BigInteger UNPAY_COIN = new BigInteger("100000", 10);

    public static String toPlainString(BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        BigDecimal valueInBTC = new BigDecimal(value).divide(new BigDecimal(UNPAY_COIN));

        return valueInBTC.toPlainString();
    }

    public static BigInteger toUnpayVaue(BigInteger value){
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        BigDecimal valueInUNP = new BigDecimal(value).divide(new BigDecimal(1000));
        
        return valueInUNP.toBigInteger();
    }
}
