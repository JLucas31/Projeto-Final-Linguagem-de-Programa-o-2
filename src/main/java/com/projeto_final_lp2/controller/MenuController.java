package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ClienteDAO;
import com.projeto_final_lp2.model.DAO.VendedorDAO;
import com.projeto_final_lp2.model.DAO.impl.ClienteDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.VendedorDAOJDBC;
import com.projeto_final_lp2.model.Entities.Cliente;
import com.projeto_final_lp2.model.Entities.Vendedor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class MenuController {

    // ==========================================
    // Componentes - Cadastro de Cliente
    // ==========================================
    @FXML
    private TextField txtClienteNome;

    @FXML
    private TextField txtClienteCpf;

    @FXML
    private TextField txtClienteEmail;

    @FXML
    private DatePicker dpClienteDataNasc;

    @FXML
    private Label lblClienteStatus;

    // ==========================================
    // Componentes - Cadastro de Vendedor
    // ==========================================
    @FXML
    private TextField txtVendedorNome;

    @FXML
    private TextField txtVendedorTelefone;

    @FXML
    private DatePicker dpVendedorDataNasc;

    @FXML
    private TextField txtVendedorSalario;

    @FXML
    private TextField txtVendedorComissao;

    @FXML
    private Label lblVendedorStatus;

    // ==========================================
    // Instâncias de DAO
    // ==========================================
    private final ClienteDAO clienteDAO = new ClienteDAOJDBC();
    private final VendedorDAO vendedorDAO = new VendedorDAOJDBC();

    // ==========================================
    // Ações - Cliente
    // ==========================================
    @FXML
    private void onSalvarCliente(ActionEvent event) {
        String nome = txtClienteNome.getText() != null ? txtClienteNome.getText().trim() : "";
        String cpf = txtClienteCpf.getText() != null ? txtClienteCpf.getText().trim() : "";
        String email = txtClienteEmail.getText() != null ? txtClienteEmail.getText().trim() : "";
        LocalDate dataNasc = dpClienteDataNasc.getValue();

        // Validação de campos obrigatórios
        if (nome.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Nome obrigatório", "Por favor, informe o nome do cliente.");
            txtClienteNome.requestFocus();
            return;
        }

        if (cpf.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "CPF obrigatório", "Por favor, informe o CPF do cliente.");
            txtClienteCpf.requestFocus();
            return;
        }

        if (dataNasc == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Data de Nascimento obrigatória", "Por favor, selecione a data de nascimento.");
            dpClienteDataNasc.requestFocus();
            return;
        }

        // Criar objeto Cliente e validar CPF
        Cliente cliente = new Cliente(nome, cpf, email, dataNasc);

        if (!cliente.validarCpf()) {
            mostrarAlerta(Alert.AlertType.ERROR, "CPF Inválido", "O CPF informado é inválido!", "Verifique os dígitos do CPF e tente novamente.");
            txtClienteCpf.requestFocus();
            return;
        }

        // Tentar salvar no Banco de Dados
        try {
            clienteDAO.cadastrarCliente(cliente);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Cliente Cadastrado",
                    "Cliente cadastrado com sucesso!\nID gerado: " + cliente.getClientID());

            if (lblClienteStatus != null) {
                lblClienteStatus.setStyle("-fx-text-fill: #16a34a;");
                lblClienteStatus.setText("✓ Cliente cadastrado com sucesso! (ID: " + cliente.getClientID() + ")");
            }

            limparCamposCliente();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro no Cadastro", "Falha ao cadastrar cliente no banco de dados", e.getMessage());
            if (lblClienteStatus != null) {
                lblClienteStatus.setStyle("-fx-text-fill: #dc2626;");
                lblClienteStatus.setText("✗ Erro: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onLimparCliente(ActionEvent event) {
        limparCamposCliente();
        if (lblClienteStatus != null) {
            lblClienteStatus.setText("");
        }
    }

    private void limparCamposCliente() {
        txtClienteNome.clear();
        txtClienteCpf.clear();
        txtClienteEmail.clear();
        dpClienteDataNasc.setValue(null);
    }

    // ==========================================
    // Ações - Vendedor
    // ==========================================
    @FXML
    private void onSalvarVendedor(ActionEvent event) {
        String nome = txtVendedorNome.getText() != null ? txtVendedorNome.getText().trim() : "";
        String telefone = txtVendedorTelefone.getText() != null ? txtVendedorTelefone.getText().trim() : "";
        LocalDate dataNasc = dpVendedorDataNasc.getValue();
        String salarioStr = txtVendedorSalario.getText() != null ? txtVendedorSalario.getText().trim().replace(",", ".") : "";
        String comissaoStr = txtVendedorComissao.getText() != null ? txtVendedorComissao.getText().trim().replace(",", ".") : "";

        // Validação de campos obrigatórios
        if (nome.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Nome obrigatório", "Por favor, informe o nome do vendedor.");
            txtVendedorNome.requestFocus();
            return;
        }

        if (salarioStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Salário Base obrigatório", "Por favor, informe o salário base.");
            txtVendedorSalario.requestFocus();
            return;
        }

        if (comissaoStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Percentual de Comissão obrigatório", "Por favor, informe o percentual de comissão.");
            txtVendedorComissao.requestFocus();
            return;
        }

        double salarioBase;
        double percentualComissao;

        try {
            salarioBase = Double.parseDouble(salarioStr);
            if (salarioBase < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Salário Inválido", "O salário base não pode ser negativo.");
                txtVendedorSalario.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", "Salário Inválido", "Digite um valor numérico válido para o salário (ex: 2500.00).");
            txtVendedorSalario.requestFocus();
            return;
        }

        try {
            percentualComissao = Double.parseDouble(comissaoStr);
            if (percentualComissao < 0 || percentualComissao > 100) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Comissão Inválida", "O percentual de comissão deve estar entre 0% e 100%.");
                txtVendedorComissao.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", "Comissão Inválida", "Digite um valor numérico válido para a comissão (ex: 5.0).");
            txtVendedorComissao.requestFocus();
            return;
        }

        // Criar objeto Vendedor
        Vendedor vendedor = new Vendedor(nome, telefone, dataNasc, salarioBase, percentualComissao);

        // Tentar salvar no Banco de Dados
        try {
            vendedorDAO.cadastrarVendedor(vendedor);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Vendedor Cadastrado",
                    "Vendedor cadastrado com sucesso!\nID gerado: " + vendedor.getVendedorID());

            if (lblVendedorStatus != null) {
                lblVendedorStatus.setStyle("-fx-text-fill: #16a34a;");
                lblVendedorStatus.setText("✓ Vendedor cadastrado com sucesso! (ID: " + vendedor.getVendedorID() + ")");
            }

            limparCamposVendedor();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro no Cadastro", "Falha ao cadastrar vendedor no banco de dados", e.getMessage());
            if (lblVendedorStatus != null) {
                lblVendedorStatus.setStyle("-fx-text-fill: #dc2626;");
                lblVendedorStatus.setText("✗ Erro: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onLimparVendedor(ActionEvent event) {
        limparCamposVendedor();
        if (lblVendedorStatus != null) {
            lblVendedorStatus.setText("");
        }
    }

    private void limparCamposVendedor() {
        txtVendedorNome.clear();
        txtVendedorTelefone.clear();
        dpVendedorDataNasc.setValue(null);
        txtVendedorSalario.clear();
        txtVendedorComissao.clear();
    }

    // ==========================================
    // Utilitário de Alerta
    // ==========================================
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
