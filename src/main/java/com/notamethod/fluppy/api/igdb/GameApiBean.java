package com.notamethod.fluppy.api.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameApiBean {
    String name;
    String url;
    Long id;
    Long cover;
    Long first_release_date;
    List<Genre> genres;
    List<involvedCompany> involved_companies;


    public GameApiBean(String name) {
        this.name=name.substring(0,1).toUpperCase()+name.substring(1,name.length() );
    }

    public String getYear(){
        if (first_release_date!=null){
            Instant instant = Instant.ofEpochSecond(first_release_date);
            ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());

            return String.valueOf(dateTime.getYear());
        }
        return null;
    }

    public String toString(){
        if (this.getYear()!=null){
            return name+" ("+getYear()+")";
        }
        return this.getName();
    }
}
