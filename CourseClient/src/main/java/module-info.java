module com.quartyom.courseclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires slf4j.api;
    requires java.sql;

    opens com.quartyom.courseclient to javafx.fxml;
    exports com.quartyom.courseclient;
}