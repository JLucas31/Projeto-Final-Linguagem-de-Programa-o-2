package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.NotaFiscalCompraDAO;
import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.DAO.impl.NotaFiscalCompraDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.ProdutoDAOJDBC;
import com.projeto_final_lp2.model.Entities.ItemNota;
import com.projeto_final_lp2.model.Entities.NotaFiscalCompra;
import com.projeto_final_lp2.model.Entities.Produto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

// Aba Compras: notas fiscais de entrada de mercadoria. Registrar uma compra
// grava a nota, soma a quantidade ao estoque e atualiza a ultima data de compra.
public class ComprasController implements Initializable {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final LocalDate DATA_MAXIMA = LocalDate.of(2100, 12, 31);

    @FXML
    private TableView<NotaFiscalCompra> tabela;
    @FXML
    private TableColumn<NotaFiscalCompra, Integer> colId;
    @FXML
    private TableColumn<NotaFiscalCompra, String> colNumero;
    @FXML
    private TableColumn<NotaFiscalCompra, String> colChave;
    @FXML
    private TableColumn<NotaFiscalCompra, String> colFornecedor;
    @FXML
    private TableColumn<NotaFiscalCompra, LocalDate> colData;
    @FXML
    private TableColumn<NotaFiscalCompra, Double> colTotal;
    @FXML
    private Label lblStatus;

    private final NotaFiscalCompraDAO notaDAO = new NotaFiscalCompraDAOJDBC();
    private final ProdutoDAO produtoDAO = new ProdutoDAOJDBC();
    private final ObservableList<NotaFiscalCompra> lista = FXCollections.observableArrayList();
    private FiltroTabela<NotaFiscalCompra> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("nota_id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colChave.setCellValueFactory(new PropertyValueFactory<>("chave_acesso"));
        colFornecedor.setCellValueFactory(new PropertyValueFactory<>("fornecedor"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data_entrada"));
        colData.setCellFactory(c -> Util.celulaData());
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valor_total"));
        colTotal.setCellFactory(c -> Util.celulaMoeda());

        // Duplo clique na linha = ver os itens da compra
        tabela.setRowFactory(tv -> {
            TableRow<NotaFiscalCompra> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) {
                    mostrarItens(linha.getItem());
                }
            });
            return linha;
        });

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+')
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, n -> String.valueOf(n.getNota_id()))
                .coluna(colNumero, NotaFiscalCompra::getNumero)
                .coluna(colChave, NotaFiscalCompra::getChave_acesso)
                .coluna(colFornecedor, NotaFiscalCompra::getFornecedor)
                .coluna(colData, n -> Util.data(n.getData_entrada()))
                .coluna(colTotal, n -> Util.moeda(n.getValor_total()));

        carregar();
    }

    private void carregar() {
        try {
            List<NotaFiscalCompra> notas = notaDAO.listarPorPeriodo(DATA_MINIMA, DATA_MAXIMA);
            lista.setAll(notas);
            lblStatus.setText(notas.size() + " compra(s) registrada(s).");
        } catch (Exception e) {
            Util.erro("Falha ao carregar as compras", e.getMessage());
        }
    }

    private void mostrarItens(NotaFiscalCompra nota) {
        StringBuilder texto = new StringBuilder();
        for (ItemNota item : nota.getItens()) {
            texto.append(String.format("%dx %s - %s%s%n",
                    item.getQtd(),
                    item.getProduto().getNomeProduto(),
                    Util.moeda(item.getValor_unitario()),
                    item.getLote() != null && !item.getLote().isBlank() ? " (lote " + item.getLote() + ")" : ""));
        }
        texto.append(String.format("%nTotal: %s", Util.moeda(nota.getValor_total())));
        Util.info("Compra " + nota.getNumero() + " - " + nota.getFornecedor(), texto.toString());
    }

    @FXML
    private void onVoltar(ActionEvent event) {
        Navegacao.irParaMenu((Node) event.getSource());
    }

    @FXML
    private void onNovaCompra(ActionEvent event) {
        List<Produto> produtos;
        try {
            produtos = produtoDAO.listarProdutos();
        } catch (Exception e) {
            Util.erro("Falha ao carregar os produtos", e.getMessage());
            return;
        }
        if (produtos.isEmpty()) {
            Util.aviso("Nenhum produto cadastrado", "Cadastre um produto na aba Produtos antes de registrar uma compra.");
            return;
        }

        // ----- Dados da nota -----
        TextField txtNumero = new TextField();
        TextField txtChave = new TextField();
        TextField txtFornecedor = new TextField();
        DatePicker dpData = new DatePicker(LocalDate.now());
        txtChave.setPromptText("até 44 caracteres");

        GridPane dadosNota = new GridPane();
        dadosNota.setHgap(10);
        dadosNota.setVgap(8);
        dadosNota.addRow(0, new Label("Número *"), txtNumero, new Label("Fornecedor *"), txtFornecedor);
        dadosNota.addRow(1, new Label("Chave de acesso *"), txtChave, new Label("Data de entrada *"), dpData);

        // ----- Itens da nota -----
        ComboBox<Produto> cbProduto = new ComboBox<>(FXCollections.observableArrayList(produtos));
        cbProduto.setConverter(new StringConverter<>() {
            @Override
            public String toString(Produto p) {
                return p == null ? "" : p.getNomeProduto() + (p.getMarca() != null ? " - " + p.getMarca() : "");
            }

            @Override
            public Produto fromString(String s) {
                return null;
            }
        });
        cbProduto.setPromptText("Produto");
        TextField txtQtd = new TextField();
        txtQtd.setPromptText("Qtd");
        txtQtd.setPrefWidth(70);
        TextField txtCusto = new TextField();
        txtCusto.setPromptText("Custo unit.");
        txtCusto.setPrefWidth(100);
        TextField txtLote = new TextField();
        txtLote.setPromptText("Lote");
        txtLote.setPrefWidth(100);
        Button btnAdicionar = new Button("Adicionar item");
        Button btnRemover = new Button("Remover item");

        ObservableList<ItemNota> itens = FXCollections.observableArrayList();
        TableView<ItemNota> tabelaItens = new TableView<>(itens);
        tabelaItens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaItens.setPrefHeight(180);

        TableColumn<ItemNota, String> cProduto = new TableColumn<>("Produto");
        cProduto.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getProduto().getNomeProduto()));
        TableColumn<ItemNota, Integer> cQtd = new TableColumn<>("Qtd");
        cQtd.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getQtd()));
        TableColumn<ItemNota, String> cCusto = new TableColumn<>("Custo unit.");
        cCusto.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(Util.moeda(d.getValue().getValor_unitario())));
        TableColumn<ItemNota, String> cLote = new TableColumn<>("Lote");
        cLote.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
                d.getValue().getLote() != null ? d.getValue().getLote() : ""));
        TableColumn<ItemNota, String> cSubtotal = new TableColumn<>("Subtotal");
        cSubtotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(Util.moeda(d.getValue().calculaSubtotal())));
        tabelaItens.getColumns().addAll(cProduto, cQtd, cCusto, cLote, cSubtotal);

        Label lblTotal = new Label("Total: R$ 0,00");

        Runnable atualizarTotal = () -> lblTotal.setText("Total: "
                + Util.moeda(itens.stream().mapToDouble(ItemNota::calculaSubtotal).sum()));

        btnAdicionar.setOnAction(e -> {
            Produto produto = cbProduto.getValue();
            if (produto == null) {
                Util.aviso("Produto obrigatório", "Selecione o produto do item.");
                return;
            }
            try {
                int qtd = Integer.parseInt(Util.texto(txtQtd));
                double custo = Double.parseDouble(Util.texto(txtCusto).replace(",", "."));
                itens.add(new ItemNota(produto, qtd, custo, Util.texto(txtLote)));
                atualizarTotal.run();
                cbProduto.setValue(null);
                txtQtd.clear();
                txtCusto.clear();
                txtLote.clear();
            } catch (NumberFormatException ex) {
                Util.aviso("Valor inválido", "Informe a quantidade (inteiro) e o custo unitário (número).");
            } catch (IllegalArgumentException ex) {
                Util.aviso("Item inválido", ex.getMessage());
            }
        });

        btnRemover.setOnAction(e -> {
            ItemNota selecionado = tabelaItens.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                itens.remove(selecionado);
                atualizarTotal.run();
            }
        });

        HBox linhaItem = new HBox(8, cbProduto, txtQtd, txtCusto, txtLote, btnAdicionar, btnRemover);

        VBox conteudo = new VBox(12, dadosNota, new Separator(), linhaItem, tabelaItens, lblTotal);
        conteudo.setPadding(new Insets(10));
        conteudo.setPrefWidth(760);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Nova Compra (nota fiscal de entrada)");
        dialogo.initOwner(tabela.getScene().getWindow());
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        boolean[] salvo = {false};

        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            if (itens.isEmpty()) {
                Util.aviso("Nota sem itens", "Adicione pelo menos um item à compra.");
                ev.consume();
                return;
            }

            try {
                NotaFiscalCompra nota = new NotaFiscalCompra(0, Util.texto(txtNumero), Util.texto(txtChave),
                        Util.texto(txtFornecedor), dpData.getValue());
                for (ItemNota item : itens) {
                    nota.adicionarItem(item);
                }
                notaDAO.registrarEntrada(nota);
                salvo[0] = true;
            } catch (Exception e) {
                Util.erro("Não foi possível registrar a compra", e.getMessage());
                ev.consume();
            }
        });

        dialogo.showAndWait();

        if (salvo[0]) {
            carregar();
        }
    }

    @FXML
    private void onVerItens(ActionEvent event) {
        NotaFiscalCompra selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Util.aviso("Nenhuma compra selecionada", "Selecione uma compra na tabela.");
            return;
        }
        mostrarItens(selecionada);
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
