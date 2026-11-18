module com.example.projeto_final_lp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.projeto_final_lp2.controller to javafx.fxml;
    opens com.projeto_final_lp2.model.Entities to javafx.base;

    exports com.projeto_final_lp2.controller;
    exports com.projeto_final_lp2.model.Entities;
}
