package com.notamethod.fluppy.gui;



import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FontUtils {

    private FontUtils() {
    }

    /**
     * Charge une police personnalisée depuis les ressources.
     * @param fontPath Chemin relatif vers le fichier .ttf (ex: "/fonts/MaPolice.ttf")
     * @param size Taille de la police
     * @return Font chargée ou police par défaut si échec
     */
    public static Font loadCustomFont(String fontPath, double size) {
        try {
            Font font = Font.loadFont(FontUtils.class.getResourceAsStream("/font/"+fontPath), size);
            if (font==null){
                log.warn("Erreur lors du chargement de la police : " + fontPath);
                return Font.getDefault();
            }
            log.info("Font loaded: "+font.getName()+"<->"+font.getFamily());
            return font;
        } catch (Exception e) {
            log.warn("Erreur lors du chargement de la police : " + fontPath,e);
            return Font.getDefault();
        }
    }
}
