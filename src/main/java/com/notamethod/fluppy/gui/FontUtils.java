package com.notamethod.fluppy.gui;



import javafx.scene.text.Font;

public class FontUtils {

    /**
     * Charge une police personnalisée depuis les ressources.
     * @param fontPath Chemin relatif vers le fichier .ttf (ex: "/fonts/MaPolice.ttf")
     * @param size Taille de la police
     * @return Font chargée ou police par défaut si échec
     */
    public static Font loadCustomFont(String fontPath, double size) {
        try {
            return Font.loadFont(FontUtils.class.getResourceAsStream("/font/"+fontPath), size);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la police : " + fontPath);
            e.printStackTrace();
            return Font.getDefault();
        }
    }
}
