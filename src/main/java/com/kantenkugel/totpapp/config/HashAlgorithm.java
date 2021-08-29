package com.kantenkugel.totpapp.config;

public enum HashAlgorithm {
    SHA1("HmacSHA1")
    ;

    private final String algorithm;

    HashAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
