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
    public static final String PLATFORM_DOS ="(13,6)";
    public static final long YEAR_98 =883687213;

    ObjectMapper mapper = new ObjectMapper();

    public List<GameApiBean> getGames(String name, int limit) throws ApiException, MappingException {
        name=name.replace("-"," ");
        name=name.replace("_"," ");
        log.info("searching game ->{}<- with limit:{}", name, limit);
        mapper = new ObjectMapper();
        String endpoint="https://api.igdb.com/v4/games";
        String body0="fields *, genres.*,involved_companies.*;\n" +
                "search \""+name+"\";";
        String body=body0+
                "where platforms="+PLATFORM_DOS+" & first_release_date<"+YEAR_98+";";

        List<GameApiBean> games =getGames(name,body,limit);
        if (games.isEmpty()){
            body=body0+
                    "where first_release_date<"+YEAR_98+";";
        }
        games =getGames(name,body,limit);
        if (games.isEmpty()){
            games =getGames(name,body0,limit);
        }

        log.debug("found {} games ", games.size());
        return games;
    }

    public List<GameApiBean> getGames(String name, String body, int limitValue) throws ApiException, MappingException {

        String limit=limitValue>0?  String.format("limit %d;", limitValue):"";
        body=body+limit;
        String endpoint="https://api.igdb.com/v4/games";

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
