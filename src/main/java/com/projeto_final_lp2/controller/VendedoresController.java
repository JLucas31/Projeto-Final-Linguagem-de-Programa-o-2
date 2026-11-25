package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.VendedorDAO;
import com.projeto_final_lp2.model.DAO.impl.VendedorDAOJDBC;
import com.projeto_final_lp2.model.Entities.Vendedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

// Aba Vendedor: cadastro e tabela de vendedores (necessarios para registrar vendas).
public class VendedoresController implements Initializable {

    @FXML
    private TableView<Vendedor> tabela;
    @FXML
    private TableColumn<Vendedor, Integer> colId;
    @FXML
    private TableColumn<Vendedor, String> colNome;
    @FXML
    private TableColumn<Vendedor, String> colTelefone;
    @FXML
    private TableColumn<Vendedor, LocalDate> colDataNasc;
    @FXML
    private TableColumn<Vendedor, Double> colSalario;
    @FXML
    private TableColumn<Vendedor, Double> colPercentual;
    @FXML
    private TableColumn<Vendedor, Double> colComissao;
    @FXML
    private Label lblStatus;

    private final VendedorDAO vendedorDAO = new VendedorDAOJDBC();
    private final ObservableList<Vendedor> lista = FXCollections.observableArrayList();
    private FiltroTabela<Vendedor> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("vendedorID"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeVendedor"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colDataNasc.setCellValueFactory(new PropertyValueFactory<>("dataNasc"));
        colDataNasc.setCellFactory(c -> Util.celulaData());
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salarioBase"));
        colSalario.setCellFactory(c -> Util.celulaMoeda());
        colPercentual.setCellValueFactory(new PropertyValueFactory<>("percentualComissao"));
        colComissao.setCellValueFactory(new PropertyValueFactory<>("totalComissao"));
        colComissao.setCellFactory(c -> Util.celulaMoeda());

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+').
        // O telefone também aceita só os números.
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, v -> String.valueOf(v.getVendedorID()))
                .coluna(colNome, Vendedor::getNomeVendedor)
                .coluna(colTelefone, v -> v.getTelefone() + " " + somenteDigitos(v.getTelefone()))
                .coluna(colDataNasc, v -> Util.data(v.getDataNasc()))
                .coluna(colSalario, v -> Util.moeda(v.getSalarioBase()))
                .coluna(colPercentual, v -> String.valueOf(v.getPercentualComissao()))
                .coluna(colComissao, v -> Util.moeda(v.getTotalComissao()));

        carregar();
    }

    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private void carregar() {
        try {
            List<Vendedor> vendedores = vendedorDAO.listarVendedores();
            lista.setAll(vendedores);
            lblStatus.setText(vendedores.size() + " vendedor(es) cadastrado(s).");
        } catch (Exception e) {
            Util.erro("Falha ao carregar os vendedores", e.getMessage());
        }
    }

    @FXML
    private void onVoltar(ActionEvent event) {
        Navegacao.irParaMenu((Node) event.getSource());
    }

    @FXML
    private void onCadastrar(ActionEvent event) {
        TextField txtNome = new TextField();
        TextField txtTelefone = new TextField();
        DatePicker dpDataNasc = new DatePicker();
        TextField txtSalario = new TextField();
        TextField txtComissao = new TextField();
        txtSalario.setPromptText("Ex: 2500.00");
        txtComissao.setPromptText("Ex: 5.0");

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Nome *"), txtNome);
        grade.addRow(1, new Label("Telefone"), txtTelefone);
        grade.addRow(2, new Label("Data de nascimento"), dpDataNasc);
        grade.addRow(3, new Label("Salário base (R$) *"), txtSalario);
        grade.addRow(4, new Label("Comissão (%) *"), txtComissao);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Cadastrar Vendedor");
        dialogo.initOwner(tabela.getScene().getWindow());
        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        boolean[] salvo = {false};

        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            String nome = Util.texto(txtNome);
            if (nome.isEmpty()) {
                Util.aviso("Nome obrigatório", "Informe o nome do vendedor.");
                ev.consume();
                return;
            }

            double salario;
            double comissao;
            try {
                salario = Double.parseDouble(Util.texto(txtSalario).replace(",", "."));
                comissao = Double.parseDouble(Util.texto(txtComissao).replace(",", "."));
            } catch (NumberFormatException e) {
                Util.aviso("Valor inválido", "Informe o salário e a comissão como números (ex: 2500.00 e 5.0).");
                ev.consume();
                return;
            }
            if (salario < 0) {
                Util.aviso("Salário inválido", "O salário base não pode ser negativo.");
                ev.consume();
                return;
            }
            if (comissao < 0 || comissao > 100) {
                Util.aviso("Comissão inválida", "O percentual de comissão deve estar entre 0% e 100%.");
                ev.consume();
                return;
            }

            try {
                Vendedor vendedor = new Vendedor(nome, Util.texto(txtTelefone), dpDataNasc.getValue(), salario, comissao);
                vendedorDAO.cadastrarVendedor(vendedor);
                salvo[0] = true;
            } catch (Exception e) {
                Util.erro("Não foi possível cadastrar o vendedor", e.getMessage());
                ev.consume();
            }
        });

        dialogo.showAndWait();

        if (salvo[0]) {
            carregar();
        }
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
