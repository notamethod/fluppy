
package com.notamethod.fluppy.core;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "preference")
public class PreferenceEntity {

    @Id
    private String id;
    private String dosboxPath;
    private boolean fullScreen=false;


}
