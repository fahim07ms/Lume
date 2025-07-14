module com.example.lume {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.jconsole;
    requires epub4j.core;
    requires javafx.web;
    requires org.jsoup;


    opens com.example.lume to javafx.fxml;
    exports com.example.lume;
    exports com.example.lume.components;
    opens com.example.lume.components to javafx.fxml;
}