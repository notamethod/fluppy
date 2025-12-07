package com.notamethod.fluppy;

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
