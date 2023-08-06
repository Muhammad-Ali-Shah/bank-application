module com.example.bank_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.sql;
    requires java.sql.rowset;
    requires kotlinx.coroutines.core;

    opens com.example.bank_app to javafx.fxml;
    exports com.example.bank_app;
}