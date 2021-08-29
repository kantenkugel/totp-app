package com.kantenkugel.totpapp.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Crypto {
    private static final int KEY_GEN_ITERATION_COUNT = 125_000;
    private static final short KEY_LENGTH = 256;
    private static final short KEY_SALT_LENGTH = 8;

    public static byte[] encode(byte[] data, SecretKey key) {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(data);
            byte[] dataHash = MessageDigest.getInstance("SHA-256").digest(data);
            ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES + iv.length + dataHash.length + ciphertext.length)
                    .putInt(iv.length)
                    .put(iv)
                    .putInt(dataHash.length)
                    .put(dataHash)
                    .put(ciphertext);
            return buffer.array();
        } catch(Exception exception) {
            throw new IllegalStateException("Current Encryption config is invalid", exception);
        }
    }

    public static byte[] decode(byte[] data, SecretKey key) throws DecryptionException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            byte[] iv = new byte[buffer.getInt()];
            buffer.get(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            byte[] dataHashExpected = new byte[buffer.getInt()];
            buffer.get(dataHashExpected);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] bytes = cipher.doFinal(ciphertext);
            byte[] dataHash = MessageDigest.getInstance("SHA-256").digest(bytes);
            if(!Arrays.equals(dataHashExpected, dataHash)) {
                throw new DecryptionException(new IllegalStateException("Data Hash did not match expected hash"));
            }
            return bytes;
        } catch(BufferUnderflowException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException exception) {
            throw new DecryptionException(exception);
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Current Decryption config is invalid", e);
        }
    }

    public static KeygenData generateNewKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[KEY_SALT_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        SecretKey key = generateKey(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH);
        byte[] keyHash = MessageDigest.getInstance("SHA-256").digest(key.getEncoded());
        String keyData = Base64.getEncoder().encodeToString(ByteBuffer
                .allocate(Integer.BYTES + 2*Short.BYTES + KEY_SALT_LENGTH + keyHash.length)
                .putInt(KEY_GEN_ITERATION_COUNT)
                .putShort(KEY_LENGTH)
                .putShort(KEY_SALT_LENGTH)
                .put(salt)
                .put(keyHash)
                .array());
        return new KeygenData(key, keyData);
    }

    public static SecretKey getKey(String password, String keyData) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(keyData));
            int iterationCount = buffer.getInt();
            short keyLength = buffer.getShort();
            byte[] salt = new byte[buffer.getShort()];
            buffer.get(salt);
            byte[] keyHashExpected = new byte[buffer.remaining()];
            buffer.get(keyHashExpected);

            SecretKey key = generateKey(password, salt, iterationCount, keyLength);
            byte[] keyHash = MessageDigest.getInstance("SHA-256").digest(key.getEncoded());
            if(Arrays.equals(keyHashExpected, keyHash)) {
                return key;
            }
        } catch(IllegalArgumentException | BufferUnderflowException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static SecretKey generateKey(String password, byte[] salt, int iterationCount, short keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static class KeygenData {
        private final SecretKey key;
        private final String keyData;

        private KeygenData(SecretKey key, String keyData) {
            this.key = key;
            this.keyData = keyData;
        }

        public SecretKey getKey() {
            return key;
        }

        public String getKeyData() {
            return keyData;
        }
    }

    public static class DecryptionException extends Exception {
        private DecryptionException(Throwable cause) {
            super(cause);
        }
    }

    private Crypto() {}
}
