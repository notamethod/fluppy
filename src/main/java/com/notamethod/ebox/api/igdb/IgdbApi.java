package com.notamethod.ebox.api.igdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notamethod.ebox.api.ApiException;
import com.notamethod.ebox.api.MappingException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
public class IgdbApi {

    static String token = System.getenv("IGDB_TOKEN");// Remplace par ton vrai token
    static String user=System.getenv("IGDB_USER");
    public static String thumbSize="t_thumb";
    public static String bigSize="t_cover_big";

    public List<Game> getGames(String name) throws ApiException, MappingException {
        log.info("searching game...{}", name);
        ObjectMapper mapper = new ObjectMapper();
        String endpoint="https://api.igdb.com/v4/games";
        String body="fields *;\n" +
                "search \""+name+"\";";
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + token)
                    .header("Client-ID", user)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e){
            throw new ApiException("getGames Error "+name, e);
        }

        List<Game> games;
        try {
            games = mapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new MappingException(e);
        }
        log.debug("found {} games ", games.size());
        return games;
    }

    public List<Cover> getCoverInfo(Long id) throws ApiException, MappingException {
        ObjectMapper mapper = new ObjectMapper();
        String endpoint="https://api.igdb.com/v4/covers";
        String body="fields *;\n" +
                "where id="+id+";";
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + token)
                    .header("Client-ID", user)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e){
            throw new ApiException("getCoverInfo Error "+id, e);
        }

        List<Cover> covers;
        try {
            covers = mapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new MappingException(e);
        }
        log.debug("found {} covers ", covers.size());
        return covers;

    }

}
