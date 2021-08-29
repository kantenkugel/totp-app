module com.kantenkugel.totpapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires com.fasterxml.jackson.databind;

    opens com.kantenkugel.totpapp to javafx.fxml;
    opens com.kantenkugel.totpapp.config to com.fasterxml.jackson.databind;
    exports com.kantenkugel.totpapp;
}