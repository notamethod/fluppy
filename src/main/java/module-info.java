module org.example.ebox2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires ebox.api;
    requires static lombok;
    requires jakarta.persistence;
    requires org.slf4j;
    requires java.desktop;

    opens com.notamethod.ebox to javafx.fxml;
    exports com.notamethod.ebox;
    exports com.notamethod.ebox.gui;
    opens com.notamethod.ebox.gui to javafx.fxml;
}