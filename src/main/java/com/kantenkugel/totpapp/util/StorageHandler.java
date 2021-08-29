package com.kantenkugel.totpapp.util;

import com.kantenkugel.totpapp.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class StorageHandler {
    private static final String CONFIG_FILE_PATH = ".";
    private static final String CONFIG_FILE_NAME = "totpconfig.json";

    private static final ObjectMapper MAPPER = new ObjectMapper().disable(SerializationFeature.INDENT_OUTPUT);

    public static void writeConfig(Config config, String password) throws IOException {
        MAPPER.writeValue(
                Files.newOutputStream(Paths.get(CONFIG_FILE_PATH, CONFIG_FILE_NAME), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                config);
    }

    public static Config readConfig(String password) throws IOException {
        Path path = Paths.get(CONFIG_FILE_PATH, CONFIG_FILE_NAME);
        if(Files.notExists(path)) {
            return new Config(null, new ArrayList<>());
        }
        return MAPPER.readValue(Files.newInputStream(path), Config.class);
    }

    private StorageHandler() {}
}
