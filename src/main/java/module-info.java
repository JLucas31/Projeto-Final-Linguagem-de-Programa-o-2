module com.example.projeto_final_lp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens projeto_final_lp2.controller to javafx.fxml;

    exports projeto_final_lp2.controller;
}
