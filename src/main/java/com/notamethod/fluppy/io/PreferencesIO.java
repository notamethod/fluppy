
package com.notamethod.fluppy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notamethod.fluppy.core.PreferencesBean;


import java.io.File;
import java.io.IOException;

public class PreferencesIO {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static PreferencesBean load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("[INFO] Config file not found, using defaults.");
            return new PreferencesBean(); // ou PreferencesDefaults.get()
        }
        try {
            return mapper.readValue(file, PreferencesBean.class);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read config: " + e.getMessage());
            return new PreferencesBean();
        }
    }

    public static PreferencesBean load() {
        return load("prefs.json");
    }

    public static void save(PreferencesBean prefs, String path) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), prefs);
            System.out.println("[INFO] Preferences saved to " + path);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save config: " + e.getMessage());
        }
    }
}
