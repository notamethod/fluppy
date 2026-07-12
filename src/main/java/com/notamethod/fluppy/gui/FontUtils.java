package com.notamethod.fluppy.gui;



import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FontUtils {

    static final String[] FLUPPY_FONTS = {"retro-pixel-arcade.ttf",
            "MonkeyIsland-1991.ttf",
            "MonkeyIsland-1990.ttf",
            "lucasarts-scumm-menu-shadow.otf",
            "lucasarts-scumm-solid.otf",
            "lucasarts-scumm-outline.otf",
            "lucasarts-scumm-credits.otf",
            "lucasarts-scumm-subtitle-roman.otf",
            "Storyboo.ttf",
            "Retro Gaming.ttf",
            "PxPlus_IBM_VGA_8x16.ttf",
            "PixelifySans-Regular.ttf",
            "ShareTechMono-Regular.ttf"};

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
            log.debug("Font loaded: "+font.getName()+"<->"+font.getFamily());
            return font;
        } catch (Exception e) {
            log.warn("Erreur lors du chargement de la police : " + fontPath,e);
            return Font.getDefault();
        }
    }

    public static void loadCustomFonts() {
        int size = 16;
        for (String font : FLUPPY_FONTS) {
            Font.loadFont(FontUtils.class.getResourceAsStream("/font/" + font), size);
        }
        log.info("Fonts loaded: {}", FLUPPY_FONTS.length);

    }
}
