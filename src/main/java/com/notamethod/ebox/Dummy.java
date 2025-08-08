package com.notamethod.ebox;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Dummy {
    public static URL[] getImages() throws MalformedURLException {

        String basePath="C:\\Users\\Christophe\\AppData\\Roaming\\.ebox\\covers\\";
        String img6=basePath+"cover_lostpatrol1990.jpg";
        String img1=basePath+"cover_firstsamurai1991.jpg";
        String img2=basePath+"cover_lureofthetemptress1992.jpg";
        // Liste d’images

        URL[] imagePaths2 = {
                new File(img6).toURL(),
                new File(img2).toURL(),
                new File(img1).toURL()
        };
        return imagePaths2;
    }
}
//List<String> options = Arrays.asList("Rouge", "Vert", "Bleu");
//ChoiceDialog<String> dialog = new ChoiceDialog<>("Vert", options);
//dialog.setTitle("Choix de couleur");
//dialog.setHeaderText("Veuillez choisir une couleur");
//dialog.setContentText("Couleur :");
//
//Optional<String> result = dialog.showAndWait();
//result.ifPresent(color -> System.out.println("Couleur choisie : " + color));