package com.projeto_final_lp2.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Tenta carregar o FXML do caminho relativo à classe ou absoluto no classpath
        URL fxmlLocation = HelloApplication.class.getResource("menu-view.fxml");
        if (fxmlLocation == null) {
            fxmlLocation = HelloApplication.class.getResource("/projeto_final_lp2/example/projeto_final_lp2/menu-view.fxml");
        }
        if (fxmlLocation == null) {
            throw new IOException("Arquivo menu-view.fxml não foi encontrado nos recursos.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 800, 650);
        
        stage.setTitle("Sistema de Gerenciamento da Loja - Cadastros");
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
