module com.notamethod.ebox {
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

    opens com.notamethod.ebox to javafx.fxml;
    exports com.notamethod.ebox;
    exports com.notamethod.ebox.gui;
    exports com.notamethod.ebox.core;
    exports com.notamethod.ebox.api.igdb;
    opens com.notamethod.ebox.gui to javafx.fxml;
    opens com.notamethod.ebox.core to org.hibernate.orm.core;
}