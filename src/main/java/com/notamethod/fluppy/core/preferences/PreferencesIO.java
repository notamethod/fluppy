
package com.notamethod.fluppy.core.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notamethod.fluppy.core.Configuration;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.IOException;

@Slf4j
public class PreferencesIO {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static PreferencesBean load(String path) {
        File file = new File(path);
        if (!file.exists()) {
           log.info("Config file not found, using defaults.");
            return new PreferencesBean(); // ou PreferencesDefaults.get()
        }
        try {
            return mapper.readValue(file, PreferencesBean.class);
        } catch (IOException e) {
          log.error("Failed to read config: " + e.getMessage());
            return new PreferencesBean();
        }
    }

    public static PreferencesBean load() {

        return load(Configuration.appFolder+"prefs.json");
    }

    public static void save(PreferencesBean prefs, String path) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), prefs);
        } catch (IOException e) {
            log.error("Failed to save config: " + e.getMessage());
        }
    }
}
