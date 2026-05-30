package com.notamethod.fluppy.api.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Company {
    private String id;
    private String name;
    private String slug;
    private IgdbImage logo;


}
