module com.notamethod.fluppy {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.naming;
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
    requires com.fasterxml.jackson.annotation;
    requires ch.qos.logback.classic;


    exports com.notamethod.fluppy.gui;
    exports com.notamethod.fluppy.core;
    exports com.notamethod.fluppy.api.igdb;
    opens com.notamethod.fluppy.gui to javafx.fxml;
    opens com.notamethod.fluppy.core to org.hibernate.orm.core;
    exports com.notamethod.fluppy.core.preferences;
    opens com.notamethod.fluppy.core.preferences to  org.hibernate.orm.core;
    opens com.notamethod.fluppy.core.game to  org.hibernate.orm.core;
    exports com.notamethod.fluppy.core.game;
    exports com.notamethod.fluppy.core.category;
    opens com.notamethod.fluppy.core.category to org.hibernate.orm.core;
    exports com.notamethod.fluppy.emulators.dosbox;
    opens com.notamethod.fluppy.emulators.dosbox to org.hibernate.orm.core;
    exports com.notamethod.fluppy.gui.common;
    opens com.notamethod.fluppy.gui.common to javafx.fxml, org.hibernate.orm.core;
    exports com.notamethod.fluppy.emulators;
    opens com.notamethod.fluppy.emulators to org.hibernate.orm.core;
    exports com.notamethod.fluppy.emulators.amiga;
    opens com.notamethod.fluppy.emulators.amiga to org.hibernate.orm.core;
    exports com.notamethod.fluppy.util;
    opens com.notamethod.fluppy.util to org.hibernate.orm.core;
}