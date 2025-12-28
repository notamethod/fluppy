package com.notamethod.fluppy.api.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class involvedCompany {
    private Long company;
}
