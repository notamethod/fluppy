package com.notamethod.fluppy.api;

import com.notamethod.fluppy.api.igdb.Cover;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.api.igdb.Genre;
import com.notamethod.fluppy.api.igdb.IgdbApi;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.GameApp;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ApiCalls {

    IgdbApi igdbApi = new IgdbApi();

    public List<GameApiBean> findGame(String name) throws ApiException, MappingException {

        return igdbApi.getGames(name);
    }

    public String downloadImage(String imageUrl, String destinationFile) throws IOException, URISyntaxException {


        URI uri = new URI(imageUrl);
        try (InputStream in = uri.toURL().openStream();
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


    public void findAndUpdateData(GameApp beanGame, int size, GameApiBean foundGame) throws ApiException, MappingException, IOException {


        GameApiBean game = foundGame;
        beanGame.setName(game.getName());
        beanGame.setYear(game.getYear()==null?null:Integer.valueOf(game.getYear()));
        String coverFilename = "cover_" + HelperClass.sanitizeName(game.getName()) + game.getYear();

        String path=getCover(Configuration.coverFolder, coverFilename, game.getCover(), size);
        if (path!=null){
            beanGame.setImagePath(Path.of(path));
        }
        for (Genre genre : foundGame.getGenres()){
            beanGame.addGenre(genre.getSlug(), genre.getName());
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
            String imgUrl = size == 2 ? cover.getUrl().replaceAll(IgdbApi.THUMB_SIZE, IgdbApi.BIG_SIZE) : cover.getUrl();
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
        try {
            return downloadImage("https:" + cover, outputFile.getAbsolutePath() + ".jpg");
        } catch (URISyntaxException e) {
            throw new ApiException("API ERROR", e);
        }
    }
}
