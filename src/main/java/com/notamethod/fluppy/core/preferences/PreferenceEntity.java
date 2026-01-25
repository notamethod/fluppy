
package com.notamethod.fluppy.core.preferences;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
