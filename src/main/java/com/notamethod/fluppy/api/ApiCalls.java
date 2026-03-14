package com.notamethod.fluppy.api;

import com.notamethod.fluppy.api.igdb.*;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.Company;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.gui.ImageUtils;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    public byte[] getImageBytes(String imageUrl) throws IOException, URISyntaxException {
        if (imageUrl == null || imageUrl.isEmpty())
            return null;
        URI uri = new URI("https:" + imageUrl);
        try (InputStream is = uri.toURL().openStream()) {
            return is.readAllBytes();
        }
    }


    public void findAndUpdateData(GameApp beanGame, int size, GameApiBean foundGame) throws ApiException, MappingException, IOException {

        GameApiBean game = foundGame;
        beanGame.setName(game.getName());
        beanGame.setYear(game.getYear() == null ? null : Integer.valueOf(game.getYear()));


        //TODO: new
        byte[] image=getCover(game.getCover(), size);
        if (image != null) {
            beanGame.setCoverImage(image);
        }
        for (Genre genre : foundGame.getGenres()) {
            beanGame.addGenre(genre.getSlug(), genre.getName());
        }
        if (game.getInvolved_companies() != null) {
            beanGame.setPublisher(findPublisher(game.getInvolved_companies()));
        }


    }

    public Company findPublisher(List<InvolvedCompany> involvedCompanies) {
        for (InvolvedCompany company : involvedCompanies) {
            if (company.isPublisher()) {
                return findCompany(company.getCompany());
            }
        }
        return null;
    }

    private Company findCompany(Long companyID) {
        try {
            com.notamethod.fluppy.api.igdb.Company igdbCompany = igdbApi.getCompaniesFromID(companyID).getFirst();
            if (igdbCompany == null)
                return null;
            String url = Optional.of(igdbCompany)
                    .map(com.notamethod.fluppy.api.igdb.Company::getLogo)
                    .map(IgdbImage::getUrl)
                    .orElse(null);
            Company company = new Company();
            company.setName(igdbCompany.getName());
            company.setId(igdbCompany.getSlug());
            if (url != null) {
                byte[] image = getImageBytes(getImageUri(url, 2));
                if (image != null) {
                    ImageUtils.testImage(image);
                    company.setImage(image);
                }
            }
            return company;
        } catch (ApiException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCoverUri(Long coverID, int size) throws ApiException, MappingException, IOException {

        if (coverID == null || coverID == 0) {
            log.warn("no cover found for game ");
            return null;
        }
        List<Cover> covers = igdbApi.getCoverInfo(coverID);
        if (covers.isEmpty()) {
            log.warn("no cover found for game ");
            return null;
        }

        return getImageUri(covers.getFirst().getUrl(), size);
    }

    private String getImageUri(String url, int size) {
        if (url == null)
            return null;
        return size == 2 ? url.replaceAll(IgdbApi.THUMB_SIZE, IgdbApi.BIG_SIZE) : url;
    }


    public String getCover(String coverFolder, String coverFilename, Long coverID, int size) throws IOException, ApiException, MappingException {
        log.debug("search and download cover image for {}", coverFilename);
        String cover = getCoverUri(coverID, size);

        if (cover == null) {
            return null;
        }
        File outputFile = new File(coverFolder, coverFilename);
        try {
            return downloadImage("https:" + cover, outputFile.getAbsolutePath() + ".jpg");
        } catch (URISyntaxException e) {
            throw new ApiException("API ERROR", e);
        }
    }

    public byte[] getCover(Long coverID, int size) throws IOException, ApiException, MappingException {
        log.debug("search and download cover image for {}", coverID);
        String cover = getCoverUri(coverID, size);

        if (cover == null) {
            return null;
        }

        byte[] image = null;
        try {
            image = getImageBytes(getImageUri(cover, 2));
            if (image != null) {
                ImageUtils.testImage(image);

            }
            return image;
        } catch (URISyntaxException e) {
            throw new ApiException("API ERROR", e);
        }


    }


}



