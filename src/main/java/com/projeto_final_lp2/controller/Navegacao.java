package com.projeto_final_lp2.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

// Troca de telas: cada botao do menu (e o "Menu" das telas) usa estes metodos.
// Os FXML ficam em resources/com/projeto_final_lp2/controller.
public final class Navegacao {

    public static final String MENU = "menu-view.fxml";

    private Navegacao() {
    }

    public static URL localizar(String fxml) throws IOException {
        URL url = Navegacao.class.getResource(fxml);
        if (url == null) {
            throw new IOException("O arquivo " + fxml + " não foi encontrado nos recursos.");
        }
        return url;
    }

    // Substitui o conteudo da janela atual pela tela pedida
    public static void irPara(Node origem, String fxml, String titulo) {
        try {
            Parent raiz = FXMLLoader.load(localizar(fxml));
            Scene scene = origem.getScene();
            scene.setRoot(raiz);
            ((Stage) scene.getWindow()).setTitle(titulo);
        } catch (IOException | RuntimeException e) {
            Util.erro("Não foi possível abrir a tela", e.getMessage());
        }
    }

    public static void irParaMenu(Node origem) {
        irPara(origem, MENU, "Loja Gamer - Menu");
    }
}
