/*
 * DosItem.java
 *
 * Created on 7. juni 2007, 15:47
 * @author Truben
 *
 */
package com.notamethod.ebox.app;

import lombok.Getter;
import lombok.Setter;


import javax.swing.*;
import java.io.Serializable;

public class ApplicationBean implements Serializable {

    @Setter
    @Getter
    private String name = "";
    @Setter
    @Getter
    private String path = "";
    private String gamefile = "";
    @Setter
    @Getter
    private String installer = "";
    @Getter
    private String icon = "";
    @Setter
    @Getter
    private int cycles = 3000;
    @Setter
    @Getter
    private int frameskip = 0;
    @Setter
    @Getter
    private String cdrom = "";
    @Setter
    @Getter
    private String cdromLetter = "D";
    @Setter
    @Getter
    private String cdromLabel = "";
    @Setter
    @Getter
    private String floppy = "";
    @Setter
    @Getter
    private String extra = "";
    @Setter
    @Getter
    private String genre = Configuration.pref.getGenres()[Configuration.pref.getGenres().length - 1];
    @Setter
    @Getter
    private String keywords = "";
    @Setter
    @Getter
    private boolean star = false;
    private ImageIcon imageIcon;
    private int size = Configuration.pref.getIconHeight();
    @Setter
    @Getter
    private String uniqueID;
    @Setter
    @Getter
    private String year = "";
    @Setter
    @Getter
    private String developer = "";
    @Setter
    @Getter
    private String publisher = "";
    @Getter
    @Setter
    public String gamePath;

    public ImageIcon getImageIcon() {
        if(size != Configuration.pref.getIconHeight())
            imageIcon = null;
        return imageIcon;
    }

    /** Creates a new instance of DosItem */
    public ApplicationBean() {
        // Generate unique id
        setUniqueID((int)(Math.random() * 1000000) + "");
    }

    public void setIcon(String name) {
        if (!this.icon.equals(name) || size != Configuration.pref.getIconHeight()) {
            this.icon = name;
            size = Configuration.pref.getIconHeight();
            this.imageIcon = null;
        }
    }

    public String toString() {
        return getName();
    }

    public String getGame() {
        return gamefile;
    }

    public void setGame(String game) {
        this.gamefile = game;
    }

    public String toConfigString() {
        return "start game" + "\n" +
                "  uniqueid := " + getUniqueID() + "\n" +
                "  name := " + getName() + "\n" +
                "  path := " + getPath() + "\n" +
                "  game := " + getGame() + "\n" +
                "  genre := " + getGenre() + "\n" +
                "  keywords := " + getKeywords() + "\n" +
                "  installer := " + getInstaller() + "\n" +
                "  floppy := " + getFloppy() + "\n" +
                "  cdrom := " + getCdrom() + "\n" +
                "  cdromlabel := " + getCdromLabel() + "\n" +
                "  cdromletter := " + getCdromLetter() + "\n" +
                "  extra :=" + getExtra() + "\n" +
                "  icon := " + getIcon() + "\n" +
                "  cycles := " + getCycles() + "\n" +
                "  frameskip := " + getFrameskip() + "\n" +
                "  favorite := " + isStar() + "\n" +
                "  year := " + getYear() + "\n" +
                "  developer := " + getDeveloper() + "\n" +
                "  publisher := " + getPublisher() + "\n" +
                "  gamePath := " + getGamePath() + "\n" +
                "end game\n\n";
    }

    public boolean equals(ApplicationBean d) {
        if (d.getCycles() != cycles) {
            return false;
        }
        if (!d.getGame().equals(gamefile)) {
            return false;
        }
        if (!d.getIcon().equals(icon)) {
            return false;
        }
        if (!d.getPath().equals(path)) {
            return false;
        }
        if (!d.getCdrom().equals(cdrom)) {
            return false;
        }
        if (!d.getFloppy().equals(floppy)) {
            return false;
        }
        if (!d.getExtra().equals(extra)) {
            return false;
        }
        if (!d.getGenre().equals(genre)) {
            return false;
        }
        if (!d.getKeywords().equals(keywords)) {
            return false;
        }
        return true;
    }
}
