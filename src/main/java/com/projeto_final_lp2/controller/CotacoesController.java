package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.CotacaoDAO;
import com.projeto_final_lp2.model.DAO.impl.CotacaoDAOJDBC;
import com.projeto_final_lp2.model.Entities.Cotacao;
import com.projeto_final_lp2.model.Entities.StatusCotacao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

// Aba Cotação: produtos que chegaram na quantidade minima e foram colocados
// para cotacao. A quantidade a comprar so pode ser definida aqui (a cotacao
// nasce com quantidade 0, durante a venda).
public class CotacoesController implements Initializable {

    @FXML
    private TableView<Cotacao> tabela;
    @FXML
    private TableColumn<Cotacao, Integer> colId;
    @FXML
    private TableColumn<Cotacao, LocalDate> colData;
    @FXML
    private TableColumn<Cotacao, Integer> colProdutoId;
    @FXML
    private TableColumn<Cotacao, String> colNome;
    @FXML
    private TableColumn<Cotacao, Integer> colQuantidade;
    @FXML
    private TableColumn<Cotacao, String> colStatus;
    @FXML
    private Label lblStatus;

    private final CotacaoDAO cotacaoDAO = new CotacaoDAOJDBC();
    private final ObservableList<Cotacao> lista = FXCollections.observableArrayList();
    private FiltroTabela<Cotacao> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_cotacao"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data_cotacao"));
        colData.setCellFactory(c -> Util.celulaData());
        colProdutoId.setCellValueFactory(new PropertyValueFactory<>("produto_id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade_cotacao"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusTexto"));

        // Duplo clique na linha = definir a quantidade a comprar
        tabela.setRowFactory(tv -> {
            TableRow<Cotacao> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) {
                    definirQuantidade(linha.getItem());
                }
            });
            return linha;
        });

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+')
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, c -> String.valueOf(c.getId_cotacao()))
                .coluna(colData, c -> Util.data(c.getData_cotacao()))
                .coluna(colProdutoId, c -> String.valueOf(c.getProduto_id()))
                .coluna(colNome, Cotacao::getNomeProduto)
                .coluna(colQuantidade, c -> String.valueOf(c.getQuantidade_cotacao()))
                .coluna(colStatus, Cotacao::getStatusTexto);

        carregar();
    }

    private void carregar() {
        try {
            List<Cotacao> cotacoes = cotacaoDAO.listarCotacoes();
            lista.setAll(cotacoes);
            long pendentes = cotacoes.stream()
                    .filter(c -> c.getStatus() == StatusCotacao.PENDENTE).count();
            lblStatus.setText(cotacoes.size() + " cotação(ões), " + pendentes + " pendente(s).");
        } catch (Exception e) {
            Util.erro("Falha ao carregar as cotações", e.getMessage());
        }
    }

    private Cotacao selecionada() {
        Cotacao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Util.aviso("Nenhuma cotação selecionada", "Selecione uma cotação na tabela.");
        }
        return selecionada;
    }

    private void definirQuantidade(Cotacao cotacao) {
        TextInputDialog dialogo = new TextInputDialog(String.valueOf(cotacao.getQuantidade_cotacao()));
        dialogo.setTitle("Quantidade da cotação");
        dialogo.setHeaderText("Produto: " + cotacao.getNomeProduto());
        dialogo.setContentText("Quantidade a comprar:");

        Optional<String> resposta = dialogo.showAndWait();
        if (resposta.isEmpty()) {
            return;
        }

        try {
            int quantidade = Integer.parseInt(resposta.get().trim());
            cotacaoDAO.definirQuantidade(cotacao.getId_cotacao(), quantidade);
            carregar();
        } catch (NumberFormatException e) {
            Util.aviso("Quantidade inválida", "Digite um número inteiro (0 ou mais).");
        } catch (Exception e) {
            Util.erro("Não foi possível definir a quantidade", e.getMessage());
        }
    }

    private void alterarStatus(StatusCotacao status) {
        Cotacao cotacao = selecionada();
        if (cotacao == null) {
            return;
        }
        try {
            cotacaoDAO.atualizarStatus(cotacao.getId_cotacao(), status);
            carregar();
        } catch (Exception e) {
            Util.aviso("Não foi possível alterar o status", e.getMessage());
        }
    }

    @FXML
    private void onVoltar(ActionEvent event) {
        Navegacao.irParaMenu((Node) event.getSource());
    }

    @FXML
    private void onDefinirQuantidade(ActionEvent event) {
        Cotacao cotacao = selecionada();
        if (cotacao != null) {
            definirQuantidade(cotacao);
        }
    }

    @FXML
    private void onMarcarComprado(ActionEvent event) {
        alterarStatus(StatusCotacao.COMPRADO);
    }

    @FXML
    private void onMarcarPendente(ActionEvent event) {
        alterarStatus(StatusCotacao.PENDENTE);
    }

    @FXML
    private void onRemover(ActionEvent event) {
        Cotacao cotacao = selecionada();
        if (cotacao == null) {
            return;
        }
        if (!Util.confirmar("Remover cotação",
                "Remover a cotação de '" + cotacao.getNomeProduto() + "'?",
                "O produto sai da lista de cotação.")) {
            return;
        }
        try {
            cotacaoDAO.removerCotacao(cotacao.getId_cotacao());
            carregar();
        } catch (Exception e) {
            Util.erro("Não foi possível remover a cotação", e.getMessage());
        }
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
