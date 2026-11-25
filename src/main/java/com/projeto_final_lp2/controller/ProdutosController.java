package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.DAO.impl.ProdutoDAOJDBC;
import com.projeto_final_lp2.model.Entities.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

// Aba Produtos: operacoes no topo e tabela com todos os produtos.
// Editar: botao "Editar" ou duplo clique em qualquer campo da linha.
public class ProdutosController implements Initializable {

    @FXML
    private TableView<Produto> tabela;
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
    @FXML
    private TableColumn<Produto, Integer> colQuantidadeMinima;
    @FXML
    private TableColumn<Produto, LocalDate> colUltimaCompra;
    @FXML
    private Label lblStatus;

    private final ProdutoDAO produtoDAO = new ProdutoDAOJDBC();
    private final ObservableList<Produto> lista = FXCollections.observableArrayList();
    private FiltroTabela<Produto> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("produto_id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colSetor.setCellValueFactory(new PropertyValueFactory<>("setor"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setCellFactory(c -> Util.celulaMoeda());
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colQuantidadeMinima.setCellValueFactory(new PropertyValueFactory<>("quantidadeMinima"));
        colUltimaCompra.setCellValueFactory(new PropertyValueFactory<>("ultimaDataCompra"));
        colUltimaCompra.setCellFactory(c -> Util.celulaData());

        // Linha destacada quando o estoque chegou na quantidade minima;
        // duplo clique em qualquer campo da linha abre a edicao
        tabela.setRowFactory(tv -> {
            TableRow<Produto> linha = new TableRow<>() {
                @Override
                protected void updateItem(Produto produto, boolean vazio) {
                    super.updateItem(produto, vazio);
                    getStyleClass().remove("estoque-minimo");
                    if (!vazio && produto != null && produto.atingiuQuantidadeMinima()) {
                        getStyleClass().add("estoque-minimo");
                    }
                }
            };
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) {
                    abrirFormulario(linha.getItem());
                }
            });
            return linha;
        });

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+')
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, p -> String.valueOf(p.getProduto_id()))
                .coluna(colNome, Produto::getNomeProduto)
                .coluna(colMarca, Produto::getMarca)
                .coluna(colSetor, Produto::getSetor)
                .coluna(colValor, p -> Util.moeda(p.getValor()))
                .coluna(colQuantidade, p -> String.valueOf(p.getQuantidade()))
                .coluna(colQuantidadeMinima, p -> String.valueOf(p.getQuantidadeMinima()))
                .coluna(colUltimaCompra, p -> Util.data(p.getUltimaDataCompra()));

        carregar();
    }

    private void carregar() {
        try {
            List<Produto> produtos = produtoDAO.listarProdutos();
            lista.setAll(produtos);
            lblStatus.setText(produtos.size() + " produto(s) cadastrado(s). "
                    + "Linhas em amarelo estão na quantidade mínima.");
        } catch (Exception e) {
            Util.erro("Falha ao carregar produtos", e.getMessage());
        }
    }

    // Abre o formulario; produto null = cadastro. Recarrega a tabela se salvou.
    private void abrirFormulario(Produto produto) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegacao.localizar("view-editarproduto.fxml"));
            Parent raiz = loader.load();

            EditarProdutoController controller = loader.getController();
            controller.setProduto(produto);

            Stage dialogo = new Stage();
            dialogo.initModality(Modality.WINDOW_MODAL);
            dialogo.initOwner(tabela.getScene().getWindow());
            dialogo.setTitle(produto == null ? "Cadastrar Produto" : "Editar Produto");
            dialogo.setScene(new Scene(raiz));
            dialogo.setResizable(false);
            dialogo.showAndWait();

            if (controller.isSalvo()) {
                carregar();
            }
        } catch (Exception e) {
            Util.erro("Não foi possível abrir a tela do produto", e.getMessage());
        }
    }

    @FXML
    private void onVoltar(ActionEvent event) {
        Navegacao.irParaMenu((Node) event.getSource());
    }

    @FXML
    private void onCadastrar(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar(ActionEvent event) {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Util.aviso("Nenhum produto selecionado", "Selecione um produto na tabela para editar.");
            return;
        }
        abrirFormulario(selecionado);
    }

    @FXML
    private void onExcluir(ActionEvent event) {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Util.aviso("Nenhum produto selecionado", "Selecione um produto na tabela para excluir.");
            return;
        }

        if (!Util.confirmar("Confirmar exclusão",
                "Excluir o produto '" + selecionado.getNomeProduto() + "'?",
                "Produtos que já têm vendas, compras ou cotações não podem ser excluídos.")) {
            return;
        }

        try {
            produtoDAO.removerProduto(selecionado.getProduto_id());
            carregar();
        } catch (Exception e) {
            Util.erro("Não foi possível remover o produto", e.getMessage());
        }
    }

    @FXML
    private void onEstoqueMinimo(ActionEvent event) {
        try {
            List<Produto> noMinimo = produtoDAO.listarProdutos().stream()
                    .filter(Produto::atingiuQuantidadeMinima)
                    .toList();
            lista.setAll(noMinimo);
            lblStatus.setText(noMinimo.size() + " produto(s) na quantidade mínima ou abaixo dela.");
        } catch (Exception e) {
            Util.erro("Falha ao filtrar o estoque mínimo", e.getMessage());
        }
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
