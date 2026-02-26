package com.notamethod.fluppy.api.igdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
public class IgdbApi {

    private static final String ROOT_API = "https://api.igdb.com/v4";
    static String token = System.getenv("IGDB_TOKEN");
    static String user=System.getenv("IGDB_USER");
    public static final String THUMB_SIZE="t_thumb";
    public static final String BIG_SIZE="t_cover_big";

    public List<GameApiBean> getGames(String name) throws ApiException, MappingException {
        name=name.replace("-"," ");
        name=name.replace("_"," ");
        log.info("searching game ->{}<-", name);
        ObjectMapper mapper = new ObjectMapper();
        String endpoint="https://api.igdb.com/v4/games";
        String body="fields *, genres.*,involved_companies.*;\n" +
                "search \""+name+"\";";
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = igdbRequest(endpoint, body);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new ApiException("getGames Error "+name, e);
        }
        catch (Exception e){
            throw new ApiException("getGames Error "+name, e);
        }

        if (response.statusCode()>=400){
            StringBuilder sb = new StringBuilder("External API Error: ");
            sb.append("access to ").append(endpoint).append(" failed with error code ").append(response.statusCode());
            throw new ApiException(sb.toString());
        }
        List<GameApiBean> games;
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

    private HttpRequest igdbRequest(String endpoint, String body) {
       return HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + token)
                .header("Client-ID", user)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public List<Cover> getCoverInfo(Long id) throws ApiException, MappingException {
        ObjectMapper mapper = new ObjectMapper();
        String endpoint="https://api.igdb.com/v4/covers";
        String body="fields *;\n" +
                "where id="+id+";";
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = igdbRequest(endpoint, body);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new ApiException("getCoverInfo Error "+id, e);
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

    public List<Company> getCompaniesFromID(Long id) throws ApiException, MappingException {
        ObjectMapper mapper = new ObjectMapper();

        String endpoint=ROOT_API+"/companies";
        //TODO :waiting for string templates, JEP 430, 459
        String body="""
            fields description,name,parent,slug,logo.url;
            where id=${id};""".replace("${id}", String.valueOf(id));
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = igdbRequest(endpoint, body);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new ApiException("getCompanies Error "+id, e);
        }
        catch (Exception e){
            throw new ApiException("getCompanies Error "+id, e);
        }

        List<Company> companies;
        try {
            companies = mapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new MappingException(e);
        }
        log.debug("found {} companies ", companies.size());
        return companies;

    }

}
