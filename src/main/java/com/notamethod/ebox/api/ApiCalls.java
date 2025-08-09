package com.notamethod.ebox.api;

import com.notamethod.ebox.api.igdb.Cover;
import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.api.igdb.IgdbApi;
import com.notamethod.ebox.app.ApplicationBean;
import com.notamethod.ebox.app.Configuration;
import com.notamethod.ebox.app.GameApp;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class ApiCalls {

    IgdbApi igdbApi = new IgdbApi();

    public List<Game> findGame(String name) throws ApiException, MappingException {

        return igdbApi.getGames(name);
    }

    public List<Game> findGame(ApplicationBean bean) throws ApiException, MappingException {
        return findGame(bean.getName());
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

        List<Game> games = igdbApi.getGames(beanGame.getName());
        Game game = foundGame != null ? foundGame : games.get(0);
        beanGame.setName(game.getName());
        beanGame.setYear(Integer.valueOf(game.getYear()));
        String coverFilename = "cover_" +size+ game.getName().replace(" ", "").toLowerCase() + game.getYear();

        String path=getCover(Configuration.coverFolder, coverFilename, game.getCover(), size);
        beanGame.setImagePath(Path.of(path));


    }

    public String getCover(String coverFolder, String coverFilename, Long coverID, int size) throws ApiException, MappingException, IOException {

        if (coverID == null || coverID == 0){
            log.info("no cover found for game ");
        }
        List<Cover> covers = igdbApi.getCoverInfo(coverID);
        if (covers.isEmpty()) {
            log.info("no cover found for game ");
            return "";
        }
        Cover cover = covers.get(0);
        String imgUrl = size == 2 ? cover.getUrl().replaceAll(IgdbApi.thumbSize, IgdbApi.bigSize) : cover.getUrl();

        File outputFile = new File(coverFolder, coverFilename);
        String path = downloadImage("https:" + imgUrl, outputFile.getAbsolutePath() + ".jpg");
        return path;

    }

}
