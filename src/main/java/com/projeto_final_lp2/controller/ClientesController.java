package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ClienteDAO;
import com.projeto_final_lp2.model.DAO.impl.ClienteDAOJDBC;
import com.projeto_final_lp2.model.Entities.Cliente;
import com.projeto_final_lp2.model.Entities.Venda;
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

// Aba Cliente: operacoes no topo e tabela com todos os clientes.
public class ClientesController implements Initializable {

    @FXML
    private TableView<Cliente> tabela;
    @FXML
    private TableColumn<Cliente, Integer> colId;
    @FXML
    private TableColumn<Cliente, String> colNome;
    @FXML
    private TableColumn<Cliente, String> colCpf;
    @FXML
    private TableColumn<Cliente, String> colEmail;
    @FXML
    private TableColumn<Cliente, LocalDate> colDataNasc;
    @FXML
    private Label lblStatus;

    private final ClienteDAO clienteDAO = new ClienteDAOJDBC();
    private final ObservableList<Cliente> lista = FXCollections.observableArrayList();
    private FiltroTabela<Cliente> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("clientID"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colCpf.setCellValueFactory(new PropertyValueFactory<>("CPF"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDataNasc.setCellValueFactory(new PropertyValueFactory<>("dataNasc"));
        colDataNasc.setCellFactory(c -> Util.celulaData());

        // Duplo clique na linha = editar o cliente
        tabela.setRowFactory(tv -> {
            TableRow<Cliente> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) {
                    abrirFormulario(linha.getItem());
                }
            });
            return linha;
        });

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+').
        // O CPF também aceita só os números (ex.: 52998224725).
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, c -> String.valueOf(c.getClientID()))
                .coluna(colNome, Cliente::getNomeCliente)
                .coluna(colCpf, c -> c.getCPF() + " " + somenteDigitos(c.getCPF()))
                .coluna(colEmail, Cliente::getEmail)
                .coluna(colDataNasc, c -> Util.data(c.getDataNasc()));

        carregar();
    }

    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private void carregar() {
        try {
            List<Cliente> clientes = clienteDAO.listarClientes();
            lista.setAll(clientes);
            lblStatus.setText(clientes.size() + " cliente(s) cadastrado(s).");
        } catch (Exception e) {
            Util.erro("Falha ao carregar os clientes", e.getMessage());
        }
    }

    // Formulario de cadastro (cliente null) ou edicao. O dialogo so fecha no OK
    // se os dados forem validos e o banco aceitar.
    private void abrirFormulario(Cliente cliente) {
        boolean edicao = cliente != null;

        TextField txtNome = new TextField(edicao ? cliente.getNomeCliente() : "");
        TextField txtCpf = new TextField(edicao ? cliente.getCPF() : "");
        TextField txtEmail = new TextField(edicao && cliente.getEmail() != null ? cliente.getEmail() : "");
        DatePicker dpDataNasc = new DatePicker(edicao ? cliente.getDataNasc() : null);
        txtCpf.setPromptText("000.000.000-00");

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.addRow(0, new Label("Nome *"), txtNome);
        grade.addRow(1, new Label("CPF *"), txtCpf);
        grade.addRow(2, new Label("E-mail"), txtEmail);
        grade.addRow(3, new Label("Data de nascimento *"), dpDataNasc);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(edicao ? "Editar Cliente" : "Cadastrar Cliente");
        dialogo.initOwner(tabela.getScene().getWindow());
        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        boolean[] salvo = {false};

        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            String nome = Util.texto(txtNome);
            if (nome.isEmpty()) {
                Util.aviso("Nome obrigatório", "Informe o nome do cliente.");
                ev.consume();
                return;
            }
            if (dpDataNasc.getValue() == null) {
                Util.aviso("Data de nascimento obrigatória", "Selecione a data de nascimento.");
                ev.consume();
                return;
            }

            Cliente dados = new Cliente(nome, Util.texto(txtCpf), Util.texto(txtEmail), dpDataNasc.getValue());
            if (!dados.validarCpf()) {
                Util.erro("CPF inválido", "Verifique os dígitos do CPF e tente novamente.");
                ev.consume();
                return;
            }

            try {
                if (edicao) {
                    dados.setClientID(cliente.getClientID());
                    clienteDAO.atualizarCliente(dados);
                } else {
                    clienteDAO.cadastrarCliente(dados);
                }
                salvo[0] = true;
            } catch (Exception e) {
                Util.erro("Não foi possível salvar o cliente", e.getMessage());
                ev.consume();
            }
        });

        dialogo.showAndWait();

        if (salvo[0]) {
            carregar();
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
        Cliente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Util.aviso("Nenhum cliente selecionado", "Selecione um cliente na tabela para editar.");
            return;
        }
        abrirFormulario(selecionado);
    }

    @FXML
    private void onCompras(ActionEvent event) {
        Cliente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Util.aviso("Nenhum cliente selecionado", "Selecione um cliente na tabela.");
            return;
        }

        try {
            List<Venda> compras = clienteDAO.listarCompras(selecionado.getClientID());

            if (compras.isEmpty()) {
                Util.info("Compras de " + selecionado.getNomeCliente(), "Este cliente ainda não tem compras.");
                return;
            }

            StringBuilder texto = new StringBuilder();
            for (Venda venda : compras) {
                texto.append(String.format("Venda #%d - %s - %s%s%n",
                        venda.getVenda_id(),
                        Util.data(venda.getData_venda()),
                        Util.moeda(venda.getValor_total()),
                        venda.isFinalizada() ? "" : " (em aberto)"));
            }
            Util.info("Compras de " + selecionado.getNomeCliente(), texto.toString());
        } catch (Exception e) {
            Util.erro("Não foi possível listar as compras", e.getMessage());
        }
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
