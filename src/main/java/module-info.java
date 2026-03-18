module org.example.Yatrio {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.example.Yatrio to javafx.fxml;
    opens org.example.Yatrio.controllers to javafx.fxml;
    opens org.example.Yatrio.models to javafx.base;

    exports org.example.Yatrio;
    exports org.example.Yatrio.controllers;
    exports org.example.Yatrio.models;
}
