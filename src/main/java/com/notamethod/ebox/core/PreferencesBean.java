package com.notamethod.ebox.core;

/**
 * Preferences.java
 * <p>
 * Created on 8. juni 2007, 16:39
 * @author Truben
 **/

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.Scanner;

public class PreferencesBean implements Serializable {

    @Setter
    @Getter
    private String DosBoxPath = "";
    @Setter
    @Getter
    private String[] Genres;
    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return the LastUsedPath
      * @param LastUsedPath the LastUsedPath to set
     */
    @Setter
    @Getter
    private String LastUsedPath = "";
    private String KeyBoardCode = "us";
    @Setter
    @Getter
    private String Theme = "";
    @Setter
    @Getter
    private boolean KeepOpen = false;
    @Setter
    @Getter
    private boolean FullScreen = false;
    @Setter
    @Getter
    private boolean BuiltInDosBox = false;
    /**
     * -- GETTER --
     *
     *
     * -- SETTER --
     *
     @return the ShowIcons
      * @param ShowIcons the ShowIcons to set
     */

    @Setter
    @Getter
    private boolean FirstStart = true;
    @Setter
    @Getter
    private boolean CheckForUpdates = true;

    @Setter
    @Getter
    private boolean NoConcole = true;

    @Setter
    @Getter
    private int TypeOfFileDialog = 0;

    @Getter
    @Setter
    private int gamesCount = 0;

    public void writeConfig(String filename) throws IOException {
        FileWriter fstream = new FileWriter(filename);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(this.toString());
        //Close the output stream
        out.close();
    }


    public void readConfig(String filename) {
        Scanner s = new Scanner("");
        try {
            s = new Scanner(new File(filename));
        } catch (FileNotFoundException ex) {
        }
        while (s.hasNextLine()) {
            String io = s.nextLine();
            if (io.trim().startsWith("#") || io.trim().isEmpty()) {
                continue; // Comment or blank line
            }
            String parts[] = io.split(":=");
            parts[0] = parts[0].toLowerCase().trim();
            switch (parts[0]) {
                case "dosboxpath" -> DosBoxPath = parts[1].trim();

                case "fullscreen" -> FullScreen = Boolean.parseBoolean(parts[1].trim());
                case "genres" -> stringToGenres(parts[1].trim());
                case "keepopen" -> KeepOpen = Boolean.parseBoolean(parts[1].trim());
                case "typeoffiledialog" -> TypeOfFileDialog = Integer.parseInt(parts[1].trim());

                case "lastusedpath" -> LastUsedPath = parts[1].trim();
                case "keyboardcode" -> KeyBoardCode = parts[1].trim();
                case "firststart" -> FirstStart = Boolean.parseBoolean(parts[1].trim());
                case "checkforupdates" -> CheckForUpdates = Boolean.parseBoolean(parts[1].trim());
                case "usebuiltindosbox" -> BuiltInDosBox = Boolean.parseBoolean(parts[1].trim());
                case "noconcole" -> NoConcole = Boolean.parseBoolean(parts[1].trim());

                case "gamesCount" -> gamesCount = Integer.parseInt(parts[1].trim());
            }
        }
    }

    @Override
    public String toString() {

        System.out.println("[INFO] Writing preferences.");

        return "########################################################################\n"
                + "###                        D-Box' config file                        ###\n"
                + "###         If it contains errors, D-Box will overwrite it!          ###\n"
                + "### If you want to reset settings, simply delete the file or a line. ###\n"
                + "########################################################################\n\n"
                + "DosBoxPath       := " + DosBoxPath + "\n"
                + "FirstStart       := " + FirstStart + "\n"
                + "FullScreen       := " + FullScreen + "\n"
                + "Genres           := " + genresToString() + "\n"
                + "KeepOpen         := " + KeepOpen + "\n"
                + "KeyBoardCode     := " + KeyBoardCode + "\n"
                + "LastUsedPath     := " + LastUsedPath + "\n"
                + "CheckForUpdates  := " + CheckForUpdates + "\n"
                + "NoConcole        := " + NoConcole + "\n"
                + "Theme            := " + Theme + "\n"
                + "TypeOfFileDialog := " + TypeOfFileDialog + "\n"
                + "UseBuiltInDosBox := " + BuiltInDosBox+ "\n"
                + "gamesCount       := " + gamesCount;
    }

    private String genresToString() {
        StringBuilder out = new StringBuilder();
        for (String string : Genres) {
            out.append(string).append(", ");
        }
        return out.substring(0, out.length() - 2);
    }

    private void stringToGenres(String s) {
        if (s.isEmpty()) {
            return;
        } else {
            String[] splitt = s.split(",");
            for (int i = 0; i < splitt.length; i++) {
                splitt[i] = splitt[i].trim();
            }
            setGenres(splitt);
        }
    }

    /** Creates a new instance of Preferences */
    public PreferencesBean() {
        DosBoxPath = "";
        Genres = new String[]{"Action", "Adventure", "Arcade", "Board", "Platform", "Puzzle",
                    "Racing", "RPG", "Simulation", "Sports", "Strategy", "Text Based",
                    "Utility", "Unsorted"};
    }

    public void setKeyboardCountry(String country) {
        this.KeyBoardCode = translateLanguage(country, true);
    }

    public void setKeyboardCode(String country) {
        this.KeyBoardCode = country;
    }

    public int getKeyboardIndex() {
        String[] code = new String[]{"be", "br", "cf", "cz", "dk", "su", "fr",
            "gr", "hu", "it", "la", "nl", "no", "pl",
            "po", "sl", "sp", "sv", "sf", "sg", "uk",
            "us", "dv103", "yu"};
        for (int i = 0; i < code.length; i++) {
            if (code[i].equalsIgnoreCase(KeyBoardCode)) {
                return i;
            }
        }
        return 0;
    }

    public String getKeyboardCountry() {
        return translateLanguage(KeyBoardCode, false);
    }

    public String getKeyboardCode() {
        return KeyBoardCode;
    }





    /**
     * @param name The name of language or country
     * @param type true for from country to code, or false code to country
     * @return the right country or abbr
     */
    private String translateLanguage(String name, boolean type) {
        String[] country = new String[]{"Belgium", "Brazil", "Canadian-French", "Czech Republic", "Denmark", "Finland", "France", "Germany", "Hungary", "Italy", "Latin America", "Netherlands", "Norway", "Poland", "Portugal", "Slovak Republic", "Spain", "Sweden", "Switzerland (French)", "Switzerland (German)", "United Kingdom", "United States", "United States (Dvorak)", "Yugoslavia (Serbo-Croatian)"};
        String[] code = new String[]{"be", "br", "cf", "cz", "dk", "su", "fr", "gr", "hu", "it", "la", "nl", "no", "pl", "po", "sl", "sp", "sv", "sf", "sg", "uk", "us", "dv103", "yu"};
        if (type) {
            for (int i = 0; i < country.length; i++) {
                if (name.equalsIgnoreCase(country[i])) {
                    return code[i];
                }
            }
        } else {
            for (int i = 0; i < code.length; i++) {
                if (name.equalsIgnoreCase(code[i])) {
                    return country[i];
                }
            }
        }
        return name;
    }

}
