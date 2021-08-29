package com.kantenkugel.totpapp.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TotpConfig {
    private final String secret;
    private final int timeInterval;
    private final HashAlgorithm algorithm;
    private final int length;
    private final int epochStart;

    @JsonCreator
    public TotpConfig(@JsonProperty("secret") String secret, @JsonProperty("timeInterval") int timeInterval,
                      @JsonProperty("algorithm") HashAlgorithm algorithm, @JsonProperty("length") int length,
                      @JsonProperty("epochStart") int epochStart) {
        this.secret = secret;
        this.timeInterval = timeInterval;
        this.algorithm = algorithm;
        this.length = length;
        this.epochStart = epochStart;
    }

    public static TotpConfig withDefaults(String secret) {
        return new TotpConfig(secret, 30, HashAlgorithm.SHA1, 6, 0);
    }

    public String getSecret() {
        return secret;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }

    public int getLength() {
        return length;
    }

    public int getEpochStart() {
        return epochStart;
    }
}
