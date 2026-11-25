package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.DAO.impl.ProdutoDAOJDBC;
import com.projeto_final_lp2.model.Entities.Produto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

// Formulario de produto (view-editarproduto.fxml), aberto como janela modal pela
// aba Produtos. Com um produto = edicao; com null = cadastro de produto novo.
public class EditarProdutoController implements Initializable {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtMarca;
    @FXML
    private ComboBox<String> cbSetor;
    @FXML
    private TextField txtValor;
    @FXML
    private TextField txtQuantidade;
    @FXML
    private TextField txtQuantidadeMinima;

    private final ProdutoDAO produtoDAO = new ProdutoDAOJDBC();
    private Produto produto;   // null = cadastro
    private boolean salvo = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbSetor.getItems().addAll("Jogos", "Consoles", "Periféricos", "Hardware",
                "Colecionáveis", "Acessórios", "Vestuário Geek");
        txtQuantidade.setText("0");
        txtQuantidadeMinima.setText("0");
    }

    // Chamado pela aba Produtos logo depois de carregar o FXML
    public void setProduto(Produto produto) {
        this.produto = produto;

        if (produto == null) {
            lblTitulo.setText("Cadastrar Produto");
            return;
        }

        lblTitulo.setText("Editar Produto (ID " + produto.getProduto_id() + ")");
        txtNome.setText(produto.getNomeProduto());
        txtMarca.setText(produto.getMarca() != null ? produto.getMarca() : "");
        cbSetor.setValue(produto.getSetor());
        txtValor.setText(String.valueOf(produto.getValor()));
        txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
        txtQuantidadeMinima.setText(String.valueOf(produto.getQuantidadeMinima()));
    }

    public boolean isSalvo() {
        return salvo;
    }

    @FXML
    private void onSalvar(ActionEvent event) {
        String nome = Util.texto(txtNome);
        String marca = Util.texto(txtMarca);
        String setor = cbSetor.getEditor().getText() != null ? cbSetor.getEditor().getText().trim() : "";

        if (nome.isEmpty()) {
            Util.aviso("Nome obrigatório", "Informe o nome do produto.");
            txtNome.requestFocus();
            return;
        }
        if (setor.isEmpty()) {
            Util.aviso("Setor obrigatório", "Selecione ou informe o setor do produto.");
            cbSetor.requestFocus();
            return;
        }

        double valor;
        int quantidade;
        int quantidadeMinima;

        try {
            valor = Double.parseDouble(Util.texto(txtValor).replace(",", "."));
        } catch (NumberFormatException e) {
            Util.aviso("Valor inválido", "Digite um valor numérico válido (ex: 199.90).");
            txtValor.requestFocus();
            return;
        }
        if (valor < 0) {
            Util.aviso("Valor inválido", "O valor não pode ser negativo.");
            txtValor.requestFocus();
            return;
        }

        try {
            quantidade = Util.texto(txtQuantidade).isEmpty() ? 0 : Integer.parseInt(Util.texto(txtQuantidade));
        } catch (NumberFormatException e) {
            Util.aviso("Quantidade inválida", "Digite um número inteiro para a quantidade.");
            txtQuantidade.requestFocus();
            return;
        }
        if (quantidade < 0) {
            Util.aviso("Quantidade inválida", "A quantidade não pode ser negativa.");
            txtQuantidade.requestFocus();
            return;
        }

        try {
            quantidadeMinima = Util.texto(txtQuantidadeMinima).isEmpty() ? 0 : Integer.parseInt(Util.texto(txtQuantidadeMinima));
        } catch (NumberFormatException e) {
            Util.aviso("Quantidade mínima inválida", "Digite um número inteiro para a quantidade mínima.");
            txtQuantidadeMinima.requestFocus();
            return;
        }
        if (quantidadeMinima < 0) {
            Util.aviso("Quantidade mínima inválida", "A quantidade mínima não pode ser negativa.");
            txtQuantidadeMinima.requestFocus();
            return;
        }

        try {
            if (produto == null) {
                Produto novo = new Produto(nome, marca, valor, quantidade, setor);
                novo.setQuantidadeMinima(quantidadeMinima);
                produtoDAO.cadastrarProduto(novo);
            } else {
                // Objeto novo: o produto da tabela so muda depois que o banco aceitar
                Produto atualizado = new Produto(produto.getProduto_id(), nome, marca, valor,
                        quantidade, setor, produto.getUltimaDataCompra(), quantidadeMinima);
                produtoDAO.atualizarProduto(atualizado);
            }

            salvo = true;
            fechar(event);

        } catch (Exception e) {
            Util.erro("Não foi possível salvar o produto", e.getMessage());
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        fechar(event);
    }

    private void fechar(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
