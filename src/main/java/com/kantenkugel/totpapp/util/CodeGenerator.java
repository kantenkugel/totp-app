package com.kantenkugel.totpapp.util;

import com.kantenkugel.totpapp.config.TotpConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CodeGenerator {
    public static String generateCode(TotpConfig config, byte[] decryptedSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = config.getAlgorithm().getAlgorithm();
        Mac hmac = Mac.getInstance(algorithm);
        SecretKeySpec key = new SecretKeySpec(decryptedSecret, algorithm);
        hmac.init(key);

        long counter = (System.currentTimeMillis() / 1000 - config.getEpochStart()) / config.getTimeInterval();
        byte[] counterBytes = new byte[8];
        for(int i = 8; counter > 0 && i-- > 0; counter >>>= 8) {
            counterBytes[i] = (byte) counter;
        }

        byte[] encoded = hmac.doFinal(counterBytes);
        int offset = encoded[encoded.length - 1] & 0x0F;

        byte[] digitBytes = Arrays.copyOfRange(encoded, offset, offset + 4);
        digitBytes[0] &= 0x7F;
        int digit = new BigInteger(digitBytes).mod(BigInteger.TEN.pow(config.getLength())).intValueExact();
        return String.format("%0" + config.getLength() + "d", digit);
    }

    private CodeGenerator() {}
}
