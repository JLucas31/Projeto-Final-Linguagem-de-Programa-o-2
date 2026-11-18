package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.DAO.impl.ProdutoDAOJDBC;
import com.projeto_final_lp2.model.Entities.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VendedorViewController implements Initializable {

    // ==========================================
    // Componentes do Formulário de Produto
    // ==========================================
    @FXML
    private Label lblFormTitulo;

    @FXML
    private Label lblProdutoId;

    @FXML
    private TextField txtNomeProduto;

    @FXML
    private TextField txtMarca;

    @FXML
    private ComboBox<String> cbSetor;

    @FXML
    private TextField txtValor;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnCadastrar;

    @FXML
    private Button btnAtualizar;

    @FXML
    private Button btnExcluir;

    @FXML
    private Button btnLimpar;

    // ==========================================
    // Componentes da Tabela e Pesquisa
    // ==========================================
    @FXML
    private TextField txtPesquisa;

    @FXML
    private TableView<Produto> tabelaProdutos;

    @FXML
    private TableColumn<Produto, Integer> colId;

    @FXML
    private TableColumn<Produto, String> colNome;

    @FXML
    private TableColumn<Produto, String> colMarca;

    @FXML
    private TableColumn<Produto, String> colSetor;

    @FXML
    private TableColumn<Produto, Double> colValor;

    @FXML
    private TableColumn<Produto, Integer> colQuantidade;

    // ==========================================
    // DAO e Lista Observável
    // ==========================================
    private final ProdutoDAO produtoDAO = new ProdutoDAOJDBC();
    private final ObservableList<Produto> listaProdutos = FXCollections.observableArrayList();
    private Produto produtoSelecionado = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSetores();
        configurarTabela();
        carregarTabela();

        // Ouvinte de seleção na tabela para carregar dados no formulário
        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> selecionarProduto(novo)
        );
    }

    private void configurarSetores() {
        if (cbSetor != null) {
            cbSetor.getItems().addAll(
                    "Jogos",
                    "Consoles",
                    "Periféricos",
                    "Hardware",
                    "Colecionáveis",
                    "Acessórios",
                    "Vestuário Geek"
            );
        }
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("produto_id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colSetor.setCellValueFactory(new PropertyValueFactory<>("setor"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        // Formatação da coluna de preço com "R$ 0,00"
        colValor.setCellFactory(coluna -> new TableCell<Produto, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", valor).replace(".", ","));
                }
            }
        });

        tabelaProdutos.setItems(listaProdutos);
    }

    private void carregarTabela() {
        try {
            List<Produto> produtos = produtoDAO.listarProdutos();
            listaProdutos.setAll(produtos);
            if (lblStatus != null) {
                lblStatus.setStyle("-fx-text-fill: #475569;");
                lblStatus.setText("Total de produtos cadastrados: " + produtos.size());
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao carregar produtos", e.getMessage());
        }
    }

    private void selecionarProduto(Produto produto) {
        this.produtoSelecionado = produto;

        if (produto != null) {
            lblFormTitulo.setText("Editar Produto Selecionado");
            lblProdutoId.setText("ID: " + produto.getProduto_id());
            lblProdutoId.setVisible(true);

            txtNomeProduto.setText(produto.getNomeProduto());
            txtMarca.setText(produto.getMarca() != null ? produto.getMarca() : "");
            cbSetor.setValue(produto.getSetor());
            txtValor.setText(String.valueOf(produto.getValor()));
            txtQuantidade.setText(String.valueOf(produto.getQuantidade()));

            btnCadastrar.setDisable(true);
            btnAtualizar.setDisable(false);
            btnExcluir.setDisable(false);
        } else {
            limparFormulario();
        }
    }

    // ==========================================
    // Ações do Vendedor (Cadastro & Gestão)
    // ==========================================

    @FXML
    private void onCadastrarProduto(ActionEvent event) {
        String nome = txtNomeProduto.getText() != null ? txtNomeProduto.getText().trim() : "";
        String marca = txtMarca.getText() != null ? txtMarca.getText().trim() : "";
        String setor = cbSetor.getValue() != null ? cbSetor.getValue().trim() : "";
        String valorStr = txtValor.getText() != null ? txtValor.getText().trim().replace(",", ".") : "";
        String qtdStr = txtQuantidade.getText() != null ? txtQuantidade.getText().trim() : "";

        // Validações
        if (nome.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Nome obrigatório", "Informe o nome do produto.");
            txtNomeProduto.requestFocus();
            return;
        }

        if (setor.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Setor obrigatório", "Selecione ou informe o setor do produto.");
            cbSetor.requestFocus();
            return;
        }

        if (valorStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Preço obrigatório", "Informe o valor unitário do produto.");
            txtValor.requestFocus();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
            if (valor < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Preço Inválido", "O valor não pode ser negativo.");
                txtValor.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", "Preço Inválido", "Digite um valor numérico válido (ex: 199.90).");
            txtValor.requestFocus();
            return;
        }

        int quantidade = 0;
        if (!qtdStr.isEmpty()) {
            try {
                quantidade = Integer.parseInt(qtdStr);
                if (quantidade < 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Quantidade Inválida", "A quantidade não pode ser negativa.");
                    txtQuantidade.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", "Quantidade Inválida", "Digite um número inteiro para a quantidade.");
                txtQuantidade.requestFocus();
                return;
            }
        }

        Produto novoProduto = new Produto(nome, marca, valor, quantidade, setor);

        try {
            produtoDAO.cadastrarProduto(novoProduto);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto Cadastrado",
                    "Produto cadastrado com sucesso!\nID gerado: " + novoProduto.getProduto_id());

            if (lblStatus != null) {
                lblStatus.setStyle("-fx-text-fill: #16a34a;");
                lblStatus.setText("✓ Produto '" + novoProduto.getNomeProduto() + "' cadastrado com sucesso!");
            }

            carregarTabela();
            limparFormulario();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro no Cadastro", "Falha ao gravar produto no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onAtualizarProduto(ActionEvent event) {
        if (produtoSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Nenhum produto selecionado", "Selecione um produto na tabela para editar.");
            return;
        }

        String nome = txtNomeProduto.getText() != null ? txtNomeProduto.getText().trim() : "";
        String marca = txtMarca.getText() != null ? txtMarca.getText().trim() : "";
        String setor = cbSetor.getValue() != null ? cbSetor.getValue().trim() : "";
        String valorStr = txtValor.getText() != null ? txtValor.getText().trim().replace(",", ".") : "";
        String qtdStr = txtQuantidade.getText() != null ? txtQuantidade.getText().trim() : "";

        if (nome.isEmpty() || setor.isEmpty() || valorStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Campos incompletos", "Nome, setor e valor são obrigatórios.");
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr);
            int quantidade = qtdStr.isEmpty() ? 0 : Integer.parseInt(qtdStr);

            produtoSelecionado.setNomeProduto(nome);
            produtoSelecionado.setMarca(marca);
            produtoSelecionado.setSetor(setor);
            produtoSelecionado.setValor(valor);
            produtoSelecionado.setQuantidade(quantidade);

            produtoDAO.atualizarProduto(produtoSelecionado);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto Atualizado", "Dados do produto atualizados com sucesso!");

            carregarTabela();
            limparFormulario();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", "Erro de conversão numérico", "Verifique o valor e a quantidade digitados.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar produto", e.getMessage());
        }
    }

    @FXML
    private void onExcluirProduto(ActionEvent event) {
        if (produtoSelecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Nenhum produto selecionado", "Selecione um produto na tabela para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir o produto '" + produtoSelecionado.getNomeProduto() + "'?");
        confirmacao.setContentText("Esta ação não poderá ser desfeita caso o produto não possua histórico de vendas.");

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                produtoDAO.removerProduto(produtoSelecionado.getProduto_id());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto Removido", "Produto excluído com sucesso!");
                carregarTabela();
                limparFormulario();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao Excluir", "Não foi possível remover o produto", e.getMessage());
            }
        }
    }

    @FXML
    private void onLimpar(ActionEvent event) {
        limparFormulario();
    }

    private void limparFormulario() {
        this.produtoSelecionado = null;
        lblFormTitulo.setText("Cadastrar Novo Produto");
        lblProdutoId.setText("");
        lblProdutoId.setVisible(false);

        txtNomeProduto.clear();
        txtMarca.clear();
        cbSetor.setValue(null);
        txtValor.clear();
        txtQuantidade.clear();

        btnCadastrar.setDisable(false);
        btnAtualizar.setDisable(true);
        btnExcluir.setDisable(true);
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    // ==========================================
    // Filtros e Pesquisa
    // ==========================================

    @FXML
    private void onPesquisar(ActionEvent event) {
        String termo = txtPesquisa.getText() != null ? txtPesquisa.getText().trim() : "";

        if (termo.isEmpty()) {
            carregarTabela();
            return;
        }

        try {
            List<Produto> encontrados = produtoDAO.buscarPorNome(termo);
            listaProdutos.setAll(encontrados);
            if (lblStatus != null) {
                lblStatus.setText("Encontrado(s) " + encontrados.size() + " produto(s) com o termo '" + termo + "'.");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha na busca", e.getMessage());
        }
    }

    @FXML
    private void onRecarregarTabela(ActionEvent event) {
        txtPesquisa.clear();
        carregarTabela();
    }

    @FXML
    private void onListarEstoqueBaixo(ActionEvent event) {
        try {
            // Lista produtos com estoque menor ou igual a 5 unidades
            List<Produto> estoqueBaixo = produtoDAO.listarEstoqueBaixo(5);
            listaProdutos.setAll(estoqueBaixo);
            if (lblStatus != null) {
                lblStatus.setStyle("-fx-text-fill: #b45309;");
                lblStatus.setText("⚠️ Exibindo " + estoqueBaixo.size() + " produto(s) com estoque baixo (<= 5).");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao filtrar estoque baixo", e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
