package com.notamethod.fluppy.core.preferences;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
//@RegisterForReflection
public class PreferencesBean implements Serializable {

    @JsonProperty("dosBoxPath")
    private String dosBoxPath = "";

    @JsonProperty("fsuaePath")
    private String fsuaePath = "";
    @JsonProperty("kickstartPath")
    private String kickstartPath = "";
    @JsonProperty("ffmpegPath")
    private String ffmpegPath = "";
    @JsonProperty("dosBoxType")
    private String dosBoxType = "";

    @JsonProperty("genres")
    private String[] genres = PreferencesDefaults.DEFAULT_GENRES;

    @JsonProperty("lastUsedPath")
    private String lastUsedPath = "";

    @JsonProperty("keyboardCode")
    private String keyboardCode = "us";

    @JsonProperty("keepOpen")
    private boolean keepOpen = false;

    @JsonProperty("fullScreen")
    private boolean fullScreen = false;

    @JsonProperty("videoBackground")
    private boolean videoBackground = false;

    @JsonProperty("builtInDosBox")
    private boolean builtInDosBox = false;

    @JsonProperty("firstStart")
    private boolean firstStart = true;

    @JsonProperty("checkForUpdates")
    private boolean checkForUpdates = true;

    @JsonProperty("noConsole")
    private boolean noConsole = true;

    @JsonProperty("typeOfFileDialog")
    private int typeOfFileDialog = 0;


    @JsonProperty("windowHeight")
    private int windowHeight = 600;

    @JsonProperty("windowWidth")
    private int windowWidth = 800;

    @JsonProperty("gamesCount")
    private int gamesCount = 0;

    @JsonProperty("nsfw")
    private boolean nsfw = false;

    // --- Keyboard mapping ---
    private static final Map<String, String> COUNTRY_TO_CODE = Map.ofEntries(
            Map.entry("Belgium", "be"), Map.entry("Brazil", "br"), Map.entry("Canadian-French", "cf"),
            Map.entry("Czech Republic", "cz"), Map.entry("Denmark", "dk"), Map.entry("Finland", "su"),
            Map.entry("France", "fr"), Map.entry("Germany", "gr"), Map.entry("Hungary", "hu"),
            Map.entry("Italy", "it"), Map.entry("Latin America", "la"), Map.entry("Netherlands", "nl"),
            Map.entry("Norway", "no"), Map.entry("Poland", "pl"), Map.entry("Portugal", "po"),
            Map.entry("Slovak Republic", "sl"), Map.entry("Spain", "sp"), Map.entry("Sweden", "sv"),
            Map.entry("Switzerland (French)", "sf"), Map.entry("Switzerland (German)", "sg"),
            Map.entry("United Kingdom", "uk"), Map.entry("United States", "us"),
            Map.entry("United States (Dvorak)", "dv103"), Map.entry("Yugoslavia (Serbo-Croatian)", "yu")
    );

    private static final Map<String, String> CODE_TO_COUNTRY = COUNTRY_TO_CODE.entrySet()
            .stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public void setKeyboardCountry(String country) {
        this.keyboardCode = translateLanguage(country, true);
    }

    public String getKeyboardCountry() {
        return translateLanguage(keyboardCode, false);
    }

    public int getKeyboardIndex() {
        List<String> codes = new ArrayList<>(CODE_TO_COUNTRY.keySet());
        return codes.indexOf(keyboardCode.toLowerCase());
    }

    private String translateLanguage(String name, boolean fromCountry) {
        return fromCountry
                ? COUNTRY_TO_CODE.getOrDefault(name, name)
                : CODE_TO_COUNTRY.getOrDefault(name, name);
    }

    // --- Genre helpers ---
    public String genresToString() {
        return String.join(", ", genres);
    }

    public void stringToGenres(String s) {
        if (s == null || s.isEmpty()) return;
        this.genres = Arrays.stream(s.split(",")).map(String::trim).toArray(String[]::new);
    }

    public String getViewFilter() {
        return null;
    }
}
