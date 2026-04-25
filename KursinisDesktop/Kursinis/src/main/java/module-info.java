module com.example.kursinis {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.slf4j;
    requires static lombok;
    requires spring.security.crypto;
    requires java.desktop;

    opens com.example.kursinis.controllers to javafx.fxml;

    opens com.example.kursinis.entities to org.hibernate.orm.core, javafx.base, jakarta.persistence;

    opens com.example.kursinis to javafx.fxml;

    exports com.example.kursinis;
    exports com.example.kursinis.controllers;
}