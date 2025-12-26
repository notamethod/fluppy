module com.notamethod.fluppy {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires static lombok;
    requires jakarta.persistence;
    requires org.slf4j;
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.mapstruct;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;
    requires org.hibernate.orm.core;
    requires javafx.graphics;




    exports com.notamethod.fluppy.gui;
    exports com.notamethod.fluppy.core;
    exports com.notamethod.fluppy.api.igdb;
    opens com.notamethod.fluppy.gui to javafx.fxml;
    opens com.notamethod.fluppy.core to javafx.fxml, org.hibernate.orm.core;
    exports com.notamethod.fluppy.core.preferences;
    opens com.notamethod.fluppy.core.preferences to javafx.fxml, org.hibernate.orm.core;
    opens com.notamethod.fluppy.core.game to javafx.fxml, org.hibernate.orm.core;
    exports com.notamethod.fluppy.core.game;
}