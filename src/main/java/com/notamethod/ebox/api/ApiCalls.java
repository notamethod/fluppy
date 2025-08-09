package com.notamethod.ebox.api;

import com.notamethod.ebox.api.igdb.Cover;
import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.api.igdb.IgdbApi;
import com.notamethod.ebox.core.Configuration;
import com.notamethod.ebox.core.GameApp;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ApiCalls {

    IgdbApi igdbApi = new IgdbApi();

    public List<Game> findGame(String name) throws ApiException, MappingException {

        return igdbApi.getGames(name);
    }

    public String downloadImage(String imageUrl, String output) throws IOException {
        String destinationFile = output;

        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }

        log.debug("download successfull !");
        return destinationFile;
    }


    public void findAndUpdateData(GameApp beanGame, int size, Game foundGame) throws ApiException, MappingException, IOException {


        Game game = foundGame;
        beanGame.setName(game.getName());
        beanGame.setYear(game.getYear()==null?null:Integer.valueOf(game.getYear()));
        String coverFilename = "cover_" +size+ game.getName().replace(" ", "").toLowerCase() + game.getYear();

        String path=getCover(Configuration.coverFolder, coverFilename, game.getCover(), size);
        if (path!=null){
            beanGame.setImagePath(Path.of(path));
        }


    }

    public List<String> getCoverUri(Long coverID, int size) throws ApiException, MappingException, IOException {

        List<String> coversUri = new ArrayList<>();
        if (coverID == null || coverID == 0){
            log.warn("no cover found for game ");
            return coversUri;
        }
        List<Cover> covers = igdbApi.getCoverInfo(coverID);
        if (covers.isEmpty()) {
            log.warn("no cover found for game ");
            return coversUri;
        }
        for (Cover cover:covers){
            String imgUrl = size == 2 ? cover.getUrl().replaceAll(IgdbApi.thumbSize, IgdbApi.bigSize) : cover.getUrl();
            coversUri.add(imgUrl);
        }
        return coversUri;

    }




    public String getCover(String coverFolder, String coverFilename, Long coverID, int size) throws IOException, ApiException, MappingException {
        List<String> covers = getCoverUri(coverID,size);
        if (covers.isEmpty())
            return null;
        String cover = covers.get(0);
        if (cover==null){
            return null;
        }
        File outputFile = new File(coverFolder, coverFilename);
        String path = downloadImage("https:" + cover, outputFile.getAbsolutePath() + ".jpg");
        return path;
    }
}
