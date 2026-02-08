package com.notamethod.fluppy.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;

@Slf4j
public class DownloadFiles {

    public static void download(String urlFichier, String cheminDestination) throws IOException {
        URL url = new URL(urlFichier);
        HttpURLConnection connexion = (HttpURLConnection) url.openConnection();
        connexion.setRequestProperty("User-Agent", "Mozilla/5.0"); // utile pour certains serveurs

        try (InputStream in = connexion.getInputStream();
             FileOutputStream out = new FileOutputStream(cheminDestination)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void main(String[] args) {
        try {
            String url = "https://example.com/fichier.zip";
            String destination = "C:/Users/Christophe/Téléchargements/fichier.zip";
            download(url, destination);
        } catch (IOException e) {
            log.error("error", e);
        }
    }
}