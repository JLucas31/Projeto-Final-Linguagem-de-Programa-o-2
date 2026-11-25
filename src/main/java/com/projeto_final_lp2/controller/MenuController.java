package com.projeto_final_lp2.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

// Menu principal: cada botao leva para a tela da entidade (botoes de
// operacao no topo e tabela com todos os registros embaixo).
public class MenuController {

    @FXML
    private void onAbrirProdutos(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "produtos-view.fxml", "Loja Gamer - Produtos");
    }

    @FXML
    private void onAbrirVendas(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "vendas-view.fxml", "Loja Gamer - Vendas");
    }

    @FXML
    private void onAbrirCompras(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "compras-view.fxml", "Loja Gamer - Compras");
    }

    @FXML
    private void onAbrirClientes(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "clientes-view.fxml", "Loja Gamer - Cliente");
    }

    @FXML
    private void onAbrirCotacoes(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "cotacoes-view.fxml", "Loja Gamer - Cotação");
    }

    @FXML
    private void onAbrirVendedores(ActionEvent event) {
        Navegacao.irPara((Node) event.getSource(), "vendedores-view.fxml", "Loja Gamer - Vendedor");
    }
}
