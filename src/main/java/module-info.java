module ensetproject.chatbotedu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.apache.commons.lang3;
    requires org.json;
    requires okhttp3;
    requires jdk.httpserver;
    requires org.commonmark;

    opens ensetproject.chatbotedu to javafx.fxml;
    exports ensetproject.chatbotedu;

    exports ensetproject.chatbotedu.controller; // Cette ligne permet d'exporter le package controller
    // Ouvrir le package 'controller' pour que FXMLLoader puisse y accéder via reflection
    opens ensetproject.chatbotedu.controller to javafx.fxml;
}