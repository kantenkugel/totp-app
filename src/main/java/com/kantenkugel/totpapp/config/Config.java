package com.kantenkugel.totpapp.config;

import java.util.List;

public class Config {
    public static final short CURRENT_VERSION = 1;

    private short version = CURRENT_VERSION;

    private String keyData;

    private List<TotpConfig> totpConfigs;

    public Config(String keyData, List<TotpConfig> totpConfigs) {
        this.totpConfigs = totpConfigs;
    }

    public short getVersion() {
        return version;
    }

    public String getKeyData() {
        return keyData;
    }

    public List<TotpConfig> getTotpConfigs() {
        return totpConfigs;
    }

    //Constructor for Jackson
    private Config() {}
}
