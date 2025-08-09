/*
 * BoxListe.java
 *
 * Created on 8. juni 2007, 01:18
 *
 * @author Truben
 */

package com.notamethod.ebox.app;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ApplicationList implements Serializable {
    
    private final List<ApplicationBean> gamelist;

    /** Creates a new instance of BoxListe */
    public ApplicationList() {
        gamelist = new ArrayList<>();
    }

    /**
     * Searches through the list of games, and tries to find the game.
     * The name is case insensitive.
     *
     * @param name The name of the game
     * @return The name of the game, null if no game is found
     **/
    public ApplicationBean getGame(String name) {
        if(name == null)
            return null;
        
        name = name.toLowerCase();
        for(ApplicationBean item : gamelist) {
            if(item.getName().toLowerCase().equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Removes the game specified by the name
     * @param name The name of the game we want to remove
     * @return The game that we removed
     **/
    public ApplicationBean removeGame(String name) {
        name = name.toLowerCase();
        for(ApplicationBean item : gamelist) {
            if(item.getName().toLowerCase().equals(name)) {
                gamelist.remove(item);
                return item;
            }
        }
        return null;
    }

    public  void clearList() {
        gamelist.clear();
    }
    
    /**
     * Adds the game to the list
     * @param di The game we want to add
     **/
    public void addGame(ApplicationBean di) {
        gamelist.add(di);
    }
    
    /**
     * Get a list of game titles.
     * @return List of game titles in the db
     **/
    public String[] getGameList() {
        String[] temp = new String[gamelist.size()];
        int count=0;
        for(ApplicationBean item : gamelist) {
            temp[count++] = item.getName();
        }
        Arrays.sort(temp);
        return temp;
    }
    

    
    
    /**
     * Get a list of game titles. Separated by commas.
     * @param searchstr game name must include this
     * @return List of game titles in the db
     **/
    public String[] getGameList(String searchstr) {
        int count=0;
        int counter=0;
        searchstr=searchstr.toLowerCase();
        
        for(ApplicationBean item : gamelist) {
            if(item.getName().toLowerCase().contains(searchstr) ||
                    item.getKeywords().toLowerCase().contains(searchstr) ||
                    item.getGenre().toLowerCase().contains(searchstr) ||
                    item.getYear().toLowerCase().contains(searchstr) ||
                    item.getDeveloper().toLowerCase().contains(searchstr) ||
                    item.getPublisher().toLowerCase().contains(searchstr))
                
                count++;
        }
        
        String[] temp = new String[count];
            
        for(ApplicationBean item : gamelist) {
            if(item.getName().toLowerCase().contains(searchstr) ||
                    item.getKeywords().toLowerCase().contains(searchstr) ||
                    item.getGenre().toLowerCase().contains(searchstr) ||
                    item.getYear().toLowerCase().contains(searchstr) ||
                    item.getDeveloper().toLowerCase().contains(searchstr) ||
                    item.getPublisher().toLowerCase().contains(searchstr)) {
                temp[counter++] = item.getName();
            }
        }
        Arrays.sort(temp);
        return temp;
    }

    public String[] getFavoriteGameList() {
        int count=0;
        int counter=0;

        for(ApplicationBean item : gamelist)
            if(item.isStar())
                count++;

        String[] temp = new String[count];

        for(ApplicationBean item : gamelist) {
            if(item.isStar())
                temp[counter++] = item.getName();
        }
        Arrays.sort(temp);
        return temp;
    }

    /**
     * Get a list of game titles.
     * @param searchstr the genre
     * @return List of game titles in the db
     **/
    public String[] getGameListGenre(String searchstr) {

        if(searchstr.isEmpty()) {
            return getGameList();
        }

        int count=0;
        int counter=0;
        searchstr=searchstr.toLowerCase();

        for(ApplicationBean item : gamelist)
            if(item.getGenre().equalsIgnoreCase(searchstr))
                count++;

        String[] temp = new String[count];

        for(ApplicationBean item : gamelist) {
            if(item.getGenre().equalsIgnoreCase(searchstr)) {
                temp[counter++] = item.getName();
            }
        }
        Arrays.sort(temp);
        return temp;
    }
    
    /**
     * Find out how many games we have in our db
     * 
     * @return Number of games
     **/
    public int getNrGames() {
        return gamelist.size();
    }

    public String toConfigString() {
        StringBuilder out = new StringBuilder("## D-Box Game file. Do not edit if you don't know what you're doing! ##\n\n");
        for (ApplicationBean dosItem : gamelist) {
            out.append(dosItem.toConfigString());
        }
        return out.toString();
    }

    public void readConfig(String config) {
        long  times = System.currentTimeMillis();
        Scanner s = new Scanner(config);
        ApplicationBean d = null;
        int counter = 0;
        boolean isInGame = false;
        while(s.hasNextLine()) {
            counter++;
            String linje = s.nextLine().trim();

            if(linje.equals("start game")) {
                d = new ApplicationBean();
                isInGame = true;

            }
            else if(linje.equals("end game")) {
                if(d == null) {
                    System.out.println("Error in gamefile! No 'start game' before 'end game': " + counter);
                    continue;
                }
                gamelist.add(d);
                isInGame = false;

            }
            else if(isInGame) {
                try {


                    int start = linje.indexOf(":=");

                    final String keyword;
                    final String value;

                    if(start != -1) { 
                        keyword = linje.substring(0, start).trim();
                        value = linje.substring(start + 2).trim();
                    }
                    else
                        continue;

                    switch (keyword) {
                        case "genre" -> d.setGenre(value);
                        case "name" -> d.setName(value);
                        case "path" -> d.setPath(value);
                        case "year" -> d.setYear(value);
                        case "developer" -> d.setDeveloper(value);
                        case "publisher" -> d.setPublisher(value);
                        case "game" -> d.setGame(value);
                        case "keywords" -> d.setKeywords(value);
                        case "installer" -> d.setInstaller(value);
                        case "floppy" -> d.setFloppy(value);
                        case "cdrom" -> d.setCdrom(value);
                        case "cdromlabel" -> d.setCdromLabel(value);
                        case "uniqueid" -> d.setUniqueID(value);
                        case "cdromletter" -> d.setCdromLetter(value);
                        case "icon" -> d.setIcon(value);
                        case "extra" -> d.setExtra(value);
                        case "cycles" -> d.setCycles(Integer.parseInt(value));
                        case "frameskip" -> d.setFrameskip(Integer.parseInt(value));
                        case "favorite" -> d.setStar(Boolean.parseBoolean(value));
                    }
                }
                catch(Exception e) {
                    int answer = JOptionPane.showConfirmDialog(null, "Something is wrong with the game list! Have you edited it?\n\nLine " +
                         counter + ": \n" + linje + "\n\nI can continue, but beware that I will overwrite the value with something " +
                         "legal. I can also quit so you can edit the file back to correct condition.\n\nDo you want me to quit?","Error" +
                         " in game file",JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.YES_OPTION)
                        System.exit(0);
                }
            }
        }
        System.out.println("Gamelist read in " + (System.currentTimeMillis() - times) + " milliseconds.");
    }

}
