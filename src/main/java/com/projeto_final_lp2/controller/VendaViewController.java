package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.ClienteDAO;
import com.projeto_final_lp2.model.DAO.CotacaoDAO;
import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.DAO.VendaDAO;
import com.projeto_final_lp2.model.DAO.VendedorDAO;
import com.projeto_final_lp2.model.DAO.impl.ClienteDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.CotacaoDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.ProdutoDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.VendaDAOJDBC;
import com.projeto_final_lp2.model.DAO.impl.VendedorDAOJDBC;
import com.projeto_final_lp2.model.Entities.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class VendaViewController implements Initializable {

    // ==========================================
    // Componentes de Identificação
    // ==========================================
    @FXML
    private ComboBox<Cliente> cbCliente;

    @FXML
    private ComboBox<Vendedor> cbVendedor;

    // ==========================================
    // Componentes de Seleção de Produto
    // ==========================================
    @FXML
    private ComboBox<Produto> cbProduto;

    @FXML
    private TextField txtQtdItem;

    @FXML
    private Label lblEstoqueDisponivel;

    // ==========================================
    // Tabela do Carrinho
    // ==========================================
    @FXML
    private TableView<ItemVenda> tabelaItens;

    @FXML
    private TableColumn<ItemVenda, String> colProduto;

    @FXML
    private TableColumn<ItemVenda, Double> colValorUnitario;

    @FXML
    private TableColumn<ItemVenda, Integer> colQuantidade;

    @FXML
    private TableColumn<ItemVenda, Double> colSubtotal;

    @FXML
    private Label lblTotalItens;

    @FXML
    private Label lblTotalVenda;

    // ==========================================
    // Componentes de Pagamento
    // ==========================================
    @FXML
    private ComboBox<String> cbTipoPagamento;

    @FXML
    private VBox painelDinheiro;
    @FXML
    private TextField txtValorRecebido;
    @FXML
    private Label lblTroco;

    @FXML
    private VBox painelPix;
    @FXML
    private TextField txtChavePix;
    @FXML
    private TextField txtIdTransacao;

    @FXML
    private VBox painelDebito;
    @FXML
    private ComboBox<String> cbBandeiraDebito;
    @FXML
    private TextField txtDigitosDebito;

    @FXML
    private VBox painelCredito;
    @FXML
    private ComboBox<String> cbBandeiraCredito;
    @FXML
    private TextField txtDigitosCredito;
    @FXML
    private ComboBox<Integer> cbParcelasCredito;
    @FXML
    private Label lblValorParcela;
    @FXML
    private Label lblTotalComJuros;

    @FXML
    private Label lblStatusVenda;

    // ==========================================
    // DAOs e Estado da Venda
    // ==========================================
    private final ClienteDAO clienteDAO = new ClienteDAOJDBC();
    private final VendedorDAO vendedorDAO = new VendedorDAOJDBC();
    private final ProdutoDAO produtoDAO = new ProdutoDAOJDBC();
    private final VendaDAO vendaDAO = new VendaDAOJDBC();
    private final CotacaoDAO cotacaoDAO = new CotacaoDAOJDBC();

    private final ObservableList<ItemVenda> itensCarrinho = FXCollections.observableArrayList();
    private Venda vendaAtual = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabelaItens();
        configurarConversores();
        carregarDadosIniciais();
        configurarFormasPagamento();
        // O FXML não tem a propriedade "maxLength": o limite de 4 dígitos é aplicado aqui
        limitarADigitos(txtDigitosDebito, 4);
        limitarADigitos(txtDigitosCredito, 4);

        // Inicia nova venda em memória
        iniciarNovaVenda();

        // Atualização de estoque do produto selecionado
        cbProduto.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                lblEstoqueDisponivel.setText("Disponível: " + novo.getQuantidade() + " un.");
                lblEstoqueDisponivel.setStyle(novo.getQuantidade() > 0 ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            } else {
                lblEstoqueDisponivel.setText("");
            }
        });

        // Cálculo de troco ao digitar valor recebido
        txtValorRecebido.textProperty().addListener((obs, antigo, novo) -> atualizarCalculoTroco());
    }

    // O campo aceita somente números e no máximo "max" dígitos
    private void limitarADigitos(TextField campo, int max) {
        campo.setTextFormatter(new TextFormatter<String>(alteracao ->
                alteracao.getControlNewText().matches("\\d{0," + max + "}") ? alteracao : null));
    }

    private void iniciarNovaVenda() {
        vendaAtual = new Venda(cbCliente.getValue(), cbVendedor.getValue(), LocalDate.now());
        itensCarrinho.clear();
        atualizarTotais();
        if (lblStatusVenda != null) {
            lblStatusVenda.setText("Pronto para nova venda.");
            lblStatusVenda.setStyle("-fx-text-fill: #64748b;");
        }
    }

    private void configurarTabelaItens() {
        colProduto.setCellValueFactory(dado -> new SimpleStringProperty(
                dado.getValue().getProduto() != null ? dado.getValue().getProduto().getNomeProduto() : ""
        ));

        colValorUnitario.setCellValueFactory(new PropertyValueFactory<>("valor_unitario"));
        colValorUnitario.setCellFactory(c -> formatarCelulaMoeda());

        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("qtd"));

        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(c -> formatarCelulaMoeda());

        tabelaItens.setItems(itensCarrinho);
    }

    private TableCell<ItemVenda, Double> formatarCelulaMoeda() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", valor).replace(".", ","));
                }
            }
        };
    }

    private void configurarConversores() {
        cbCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getNomeCliente() + " (CPF: " + c.getCPF() + ")";
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        cbVendedor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vendedor v) {
                return v == null ? "" : v.getNomeVendedor() + " (ID: " + v.getVendedorID() + " - " + v.getPercentualComissao() + "%)";
            }

            @Override
            public Vendedor fromString(String string) {
                return null;
            }
        });

        cbProduto.setConverter(new StringConverter<>() {
            @Override
            public String toString(Produto p) {
                return p == null ? "" : p.getNomeProduto() + " - " + (p.getMarca() != null ? p.getMarca() : "")
                        + " | R$ " + String.format("%.2f", p.getValor()) + " (Estoque: " + p.getQuantidade() + ")";
            }

            @Override
            public Produto fromString(String string) {
                return null;
            }
        });
    }

    private void carregarDadosIniciais() {
        try {
            List<Cliente> clientes = clienteDAO.listarClientes();
            cbCliente.getItems().setAll(clientes);

            List<Vendedor> vendedores = vendedorDAO.listarVendedores();
            cbVendedor.getItems().setAll(vendedores);

            List<Produto> produtos = produtoDAO.listarProdutos();
            cbProduto.getItems().setAll(produtos);

            if (!clientes.isEmpty()) cbCliente.getSelectionModel().selectFirst();
            if (!vendedores.isEmpty()) cbVendedor.getSelectionModel().selectFirst();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao carregar dados", e.getMessage());
        }
    }

    private void configurarFormasPagamento() {
        cbTipoPagamento.getItems().addAll("Dinheiro", "Pix", "Cartão de Débito", "Cartão de Crédito");
        cbTipoPagamento.getSelectionModel().selectFirst();

        cbBandeiraDebito.getItems().addAll("Visa", "Mastercard", "Elo", "Hipercard");
        cbBandeiraDebito.getSelectionModel().selectFirst();

        cbBandeiraCredito.getItems().addAll("Visa", "Mastercard", "Elo", "Hipercard");
        cbBandeiraCredito.getSelectionModel().selectFirst();

        for (int i = 1; i <= 12; i++) {
            cbParcelasCredito.getItems().add(i);
        }
        cbParcelasCredito.getSelectionModel().selectFirst();

        cbTipoPagamento.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            alternarPainelPagamento(novo);
        });

        cbParcelasCredito.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            atualizarCalculoParcelas();
        });

        alternarPainelPagamento("Dinheiro");
    }

    private void alternarPainelPagamento(String tipo) {
        painelDinheiro.setVisible("Dinheiro".equals(tipo));
        painelDinheiro.setManaged("Dinheiro".equals(tipo));

        painelPix.setVisible("Pix".equals(tipo));
        painelPix.setManaged("Pix".equals(tipo));
        if ("Pix".equals(tipo) && (txtIdTransacao.getText() == null || txtIdTransacao.getText().isEmpty())) {
            txtIdTransacao.setText("TXN-" + System.currentTimeMillis());
            if (txtChavePix.getText() == null || txtChavePix.getText().isEmpty()) {
                txtChavePix.setText("pix@lojagamer.com.br");
            }
        }

        painelDebito.setVisible("Cartão de Débito".equals(tipo));
        painelDebito.setManaged("Cartão de Débito".equals(tipo));

        painelCredito.setVisible("Cartão de Crédito".equals(tipo));
        painelCredito.setManaged("Cartão de Crédito".equals(tipo));
        if ("Cartão de Crédito".equals(tipo)) {
            atualizarCalculoParcelas();
        }
    }

    // ==========================================
    // Ações do Carrinho
    // ==========================================

    @FXML
    private void onAdicionarItem(ActionEvent event) {
        Produto produto = cbProduto.getValue();
        if (produto == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Produto", "Nenhum produto selecionado", "Selecione um produto da lista.");
            return;
        }

        String qtdTexto = txtQtdItem.getText() != null ? txtQtdItem.getText().trim() : "1";
        int qtd;
        try {
            qtd = Integer.parseInt(qtdTexto);
            if (qtd <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Quantidade", "Quantidade inválida", "A quantidade deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato", "Quantidade inválida", "Digite um número inteiro.");
            return;
        }

        try {
            vendaAtual.inserirItem(produto, qtd);
            itensCarrinho.setAll(vendaAtual.getItens());
            atualizarTotais();
            txtQtdItem.setText("1");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Estoque", "Não foi possível adicionar o produto", e.getMessage());
        }
    }

    @FXML
    private void onRemoverItem(ActionEvent event) {
        ItemVenda selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um item", "Selecione um produto no carrinho para remover.");
            return;
        }

        vendaAtual.removerItem(selecionado.getProduto().getProduto_id());
        itensCarrinho.setAll(vendaAtual.getItens());
        atualizarTotais();
    }

    @FXML
    private void onLimparCarrinho(ActionEvent event) {
        iniciarNovaVenda();
    }

    private void atualizarTotais() {
        double total = vendaAtual.getValor_total();
        lblTotalVenda.setText(String.format("R$ %.2f", total).replace(".", ","));
        lblTotalItens.setText("Qtd. de Itens: " + vendaAtual.getItens().size());

        atualizarCalculoTroco();
        atualizarCalculoParcelas();
    }

    private void atualizarCalculoTroco() {
        if (!"Dinheiro".equals(cbTipoPagamento.getValue())) return;

        String recebidoStr = txtValorRecebido.getText() != null ? txtValorRecebido.getText().trim().replace(",", ".") : "";
        if (recebidoStr.isEmpty()) {
            lblTroco.setText("Troco: R$ 0,00");
            lblTroco.setStyle("-fx-text-fill: #475569;");
            return;
        }

        try {
            double recebido = Double.parseDouble(recebidoStr);
            double total = vendaAtual.getValor_total();
            double troco = recebido - total;

            if (troco >= 0) {
                lblTroco.setText(String.format("Troco: R$ %.2f", troco).replace(".", ","));
                lblTroco.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            } else {
                lblTroco.setText("Faltam: " + String.format("R$ %.2f", Math.abs(troco)).replace(".", ","));
                lblTroco.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException ignored) {
            lblTroco.setText("Valor inválido");
            lblTroco.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    private void atualizarCalculoParcelas() {
        if (!"Cartão de Crédito".equals(cbTipoPagamento.getValue())) return;

        double total = vendaAtual.getValor_total();
        Integer parcelas = cbParcelasCredito.getValue();
        if (parcelas == null || parcelas <= 0 || total <= 0) {
            lblValorParcela.setText("Parcela: R$ 0,00");
            lblTotalComJuros.setText("Total: R$ 0,00");
            return;
        }

        try {
            PagamentoCredito creditoTemp = new PagamentoCredito(total, "Visa", "0000", parcelas);
            double totalFinal = creditoTemp.calcularValorFinal();
            double parcela = creditoTemp.calcularValorParcela();

            lblValorParcela.setText(String.format("%dx de R$ %.2f", parcelas, parcela).replace(".", ","));
            if (creditoTemp.temJuros()) {
                lblTotalComJuros.setText("Total c/ Juros (2% a.m.): " + String.format("R$ %.2f", totalFinal).replace(".", ","));
                lblTotalComJuros.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
            } else {
                lblTotalComJuros.setText("Sem juros");
                lblTotalComJuros.setStyle("-fx-text-fill: #16a34a;");
            }
        } catch (Exception ignored) {
        }
    }

    // ==========================================
    // Finalização da Venda & Emissão da Nota
    // ==========================================

    @FXML
    private void onFinalizarVenda(ActionEvent event) {
        // Validação de itens
        if (vendaAtual.getItens().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Carrinho Vazio", "Adicione produtos", "Adicione pelo menos um produto ao carrinho antes de finalizar a venda.");
            return;
        }

        // Validação de cliente e vendedor
        Cliente cliente = cbCliente.getValue();
        if (cliente == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cliente", "Selecione o Cliente", "Selecione um cliente para a venda.");
            cbCliente.requestFocus();
            return;
        }

        Vendedor vendedor = cbVendedor.getValue();
        if (vendedor == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Vendedor", "Selecione o Vendedor", "Selecione um vendedor responsável.");
            cbVendedor.requestFocus();
            return;
        }

        vendaAtual.setCliente(cliente);
        vendaAtual.setVendedor(vendedor);
        vendaAtual.setData_venda(LocalDate.now());

        // Construção do Pagamento com base na forma escolhida
        Pagamento pagamento;
        String tipo = cbTipoPagamento.getValue();
        double totalBase = vendaAtual.getValor_total();

        try {
            switch (tipo) {
                case "Dinheiro" -> {
                    String recebidoStr = txtValorRecebido.getText() != null ? txtValorRecebido.getText().trim().replace(",", ".") : "";
                    if (recebidoStr.isEmpty()) {
                        mostrarAlerta(Alert.AlertType.WARNING, "Pagamento", "Valor recebido obrigatório", "Informe a quantia em dinheiro entregue pelo cliente.");
                        txtValorRecebido.requestFocus();
                        return;
                    }
                    double recebido = Double.parseDouble(recebidoStr);
                    if (recebido < totalBase) {
                        mostrarAlerta(Alert.AlertType.WARNING, "Pagamento", "Valor Insuficiente", "O valor recebido é menor que o total da venda.");
                        txtValorRecebido.requestFocus();
                        return;
                    }
                    pagamento = new PagamentoDinheiro(totalBase, recebido);
                }
                case "Pix" -> {
                    String chave = txtChavePix.getText() != null ? txtChavePix.getText().trim() : "";
                    String txn = txtIdTransacao.getText() != null ? txtIdTransacao.getText().trim() : "";
                    if (chave.isEmpty()) {
                        mostrarAlerta(Alert.AlertType.WARNING, "Pagamento", "Chave Pix obrigatória", "Informe a chave Pix.");
                        txtChavePix.requestFocus();
                        return;
                    }
                    pagamento = new PagamentoPix(totalBase, chave, txn.isEmpty() ? "TXN-" + System.currentTimeMillis() : txn);
                }
                case "Cartão de Débito" -> {
                    String bandeira = cbBandeiraDebito.getValue();
                    String digitos = txtDigitosDebito.getText() != null ? txtDigitosDebito.getText().trim() : "";
                    if (digitos.length() != 4 || !digitos.matches("\\d{4}")) {
                        mostrarAlerta(Alert.AlertType.WARNING, "Pagamento", "Cartão de Débito", "Informe exatamente os 4 últimos dígitos do cartão.");
                        txtDigitosDebito.requestFocus();
                        return;
                    }
                    pagamento = new PagamentoDebito(totalBase, bandeira, digitos);
                }
                case "Cartão de Crédito" -> {
                    String bandeira = cbBandeiraCredito.getValue();
                    String digitos = txtDigitosCredito.getText() != null ? txtDigitosCredito.getText().trim() : "";
                    Integer parcelas = cbParcelasCredito.getValue();
                    if (digitos.length() != 4 || !digitos.matches("\\d{4}")) {
                        mostrarAlerta(Alert.AlertType.WARNING, "Pagamento", "Cartão de Crédito", "Informe exatamente os 4 últimos dígitos do cartão.");
                        txtDigitosCredito.requestFocus();
                        return;
                    }
                    pagamento = new PagamentoCredito(totalBase, bandeira, digitos, parcelas != null ? parcelas : 1);
                }
                default -> throw new IllegalStateException("Forma de pagamento não suportada: " + tipo);
            }

            // Validação de processamento do pagamento
            if (!pagamento.processar()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Pagamento Rejeitado", "Não foi possível processar o pagamento", "Verifique as informações fornecidas.");
                return;
            }

            // Registra pagamento na venda
            vendaAtual.registrarPagamento(pagamento);

            // Finaliza venda no modelo (baixa estoque em memória)
            vendaAtual.finalizarVenda();

            // Grava no banco MySQL (venda, itens, pagamento e baixa no estoque do banco)
            vendaDAO.inserirVenda(vendaAtual);

            // Estoque ja foi baixado no banco: avisa os produtos que chegaram
            // na quantidade minima e pergunta se vao para a cotacao
            verificarEstoqueMinimo(vendaAtual);

            // Credita a comissão da venda ao vendedor
            double novaComissao = vendedorDAO.atualizarComissao(vendedor.getVendedorID());
            vendedor.adicionarVenda(vendaAtual.getVenda_id());

            // Calcula a comissão gerada nesta venda específica
            double comissaoDestaVenda = vendaAtual.getValor_total() * (vendedor.getPercentualComissao() / 100.0);

            // Emite e exibe a Nota Fiscal / Cupom Fiscal
            String textoNotaFiscal = gerarTextoNotaFiscal(vendaAtual, pagamento, comissaoDestaVenda, novaComissao);
            salvarNotaFiscalEmArquivo(vendaAtual.getVenda_id(), textoNotaFiscal);
            exibirDialogoNotaFiscal(textoNotaFiscal);

            // Atualiza listas do sistema
            carregarDadosIniciais();
            iniciarNovaVenda();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro na Finalização", "Falha ao concluir a venda no sistema", e.getMessage());
        }
    }

    // ==========================================
    // Controle de estoque mínimo (Cotação)
    // ==========================================

    // Para cada produto da venda, relê a quantidade já baixada no banco. Se
    // ela chegou na quantidade mínima (ou ficou abaixo), pergunta se o produto
    // deve ir para a aba Cotação. "Não" não faz nada e a venda segue normal.
    private void verificarEstoqueMinimo(Venda venda) {
        for (ItemVenda item : venda.getItens()) {
            try {
                Produto atual = produtoDAO.buscarPorID(item.getProduto().getProduto_id());
                if (atual == null || !atual.atingiuQuantidadeMinima()) {
                    continue;
                }

                ButtonType sim = new ButtonType("Sim", ButtonBar.ButtonData.YES);
                ButtonType nao = new ButtonType("Não", ButtonBar.ButtonData.NO);

                Alert pergunta = new Alert(Alert.AlertType.CONFIRMATION,
                        "O produto " + atual.getNomeProduto()
                                + " está com a quantidade mínima. Deseja colocá-lo para cotação?",
                        sim, nao);
                pergunta.setTitle("Estoque mínimo");
                pergunta.setHeaderText("Estoque atual: " + atual.getQuantidade()
                        + " un. (mínimo: " + atual.getQuantidadeMinima() + ")");

                if (pergunta.showAndWait().orElse(nao) == sim) {
                    cotacaoDAO.solicitarCotacao(atual.getProduto_id());
                }
            } catch (Exception e) {
                // A venda ja foi gravada: um problema na cotacao nao a desfaz
                mostrarAlerta(Alert.AlertType.WARNING, "Cotação",
                        "Não foi possível verificar a cotação de '"
                                + item.getProduto().getNomeProduto() + "'", e.getMessage());
            }
        }
    }

    // ==========================================
    // Gerador de Nota Fiscal
    // ==========================================

    private String gerarTextoNotaFiscal(Venda venda, Pagamento pagamento, double comissaoVenda, double totalComissaoVendedor) {
        StringBuilder sb = new StringBuilder();
        String linha = "========================================================================\n";
        String divisao = "------------------------------------------------------------------------\n";

        sb.append(linha);
        sb.append("                       LOJA GAMER & GEEK LTDA                          \n");
        sb.append("            CNPJ: 12.345.678/0001-99   |   IE: 987.654.321.000          \n");
        sb.append("                  Rua dos Jogos, 100 - Shopping Geek                    \n");
        sb.append(linha);
        sb.append(String.format(" NOTA FISCAL DE CONSUMIDOR ELETRÔNICA (NFC-e)  |  VENDA Nº: #%06d\n", venda.getVenda_id()));
        sb.append(String.format(" DATA E HORA DE EMISSÃO: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        sb.append(divisao);
        sb.append(String.format(" CLIENTE : %s\n", venda.getCliente().getNomeCliente()));
        sb.append(String.format(" CPF     : %s\n", venda.getCliente().getCPF()));
        if (venda.getCliente().getEmail() != null && !venda.getCliente().getEmail().isBlank()) {
            sb.append(String.format(" E-MAIL  : %s\n", venda.getCliente().getEmail()));
        }
        sb.append(String.format(" VENDEDOR: %s (ID: %d)\n", venda.getVendedor().getNomeVendedor(), venda.getVendedor().getVendedorID()));
        sb.append(divisao);
        sb.append(String.format(" %-4s %-30s %5s %12s %14s\n", "ITEM", "DESCRIÇÃO DO PRODUTO", "QTD", "VL.UNIT(R$)", "SUBTOTAL(R$)"));
        sb.append(divisao);

        int i = 1;
        for (ItemVenda item : venda.getItens()) {
            String nome = item.getProduto().getNomeProduto();
            if (nome.length() > 30) {
                nome = nome.substring(0, 27) + "...";
            }
            sb.append(String.format(" %03d  %-30s %5d %12.2f %14.2f\n",
                    i++,
                    nome,
                    item.getQtd(),
                    item.getValor_unitario(),
                    item.calculaSubtotal()));
        }

        sb.append(divisao);
        sb.append(String.format(" TOTAL DE ITENS VENDIDOS: %d\n", venda.getItens().size()));
        sb.append(String.format(" VALOR TOTAL DA VENDA   : R$ %.2f\n", venda.getValor_total()));
        sb.append(divisao);

        // Detalhes do Pagamento
        sb.append(" FORMA DE PAGAMENTO:\n");
        if (pagamento instanceof PagamentoDinheiro din) {
            sb.append(String.format("   Modalidade     : DINHEIRO\n"));
            sb.append(String.format("   Valor Entregue : R$ %.2f\n", din.getValor_recebido()));
            sb.append(String.format("   Troco          : R$ %.2f\n", din.calcularTroco()));
        } else if (pagamento instanceof PagamentoPix pix) {
            sb.append(String.format("   Modalidade     : PIX\n"));
            sb.append(String.format("   Chave Pix      : %s\n", pix.getChave_pix()));
            sb.append(String.format("   ID Transação   : %s\n", pix.getId_transacao()));
        } else if (pagamento instanceof PagamentoDebito deb) {
            sb.append(String.format("   Modalidade     : CARTÃO DE DÉBITO\n"));
            sb.append(String.format("   Bandeira       : %s\n", deb.getBandeira()));
            sb.append(String.format("   Cartão Final   : **** **** **** %s\n", deb.getUltimos_digitos()));
        } else if (pagamento instanceof PagamentoCredito cred) {
            sb.append(String.format("   Modalidade     : CARTÃO DE CRÉDITO\n"));
            sb.append(String.format("   Bandeira       : %s\n", cred.getBandeira()));
            sb.append(String.format("   Cartão Final   : **** **** **** %s\n", cred.getUltimos_digitos()));
            sb.append(String.format("   Parcelamento   : %dx de R$ %.2f\n", cred.getNumero_parcelas(), cred.calcularValorParcela()));
            if (cred.temJuros()) {
                sb.append(String.format("   Total c/ Juros : R$ %.2f (Taxa: 2%% a.m.)\n", cred.calcularValorFinal()));
            }
        }

        sb.append(divisao);
        sb.append(" CRÉDITO AO VENDEDOR:\n");
        sb.append(String.format("   Comissão da Venda : R$ %.2f (%.1f%%)\n", comissaoVenda, venda.getVendedor().getPercentualComissao()));
        sb.append(String.format("   Total Comissões   : R$ %.2f acumulado\n", totalComissaoVendedor));
        sb.append(linha);
        sb.append("    ESTOQUE DOS PRODUTOS BAIXADO COM SUCESSO NO SISTEMA E MYSQL         \n");
        sb.append("             OBRIGADO PELA PREFERÊNCIA! VOLTE SEMPRE!                   \n");
        sb.append(linha);

        return sb.toString();
    }

    private void salvarNotaFiscalEmArquivo(int vendaId, String textoNota) {
        try {
            Files.createDirectories(Paths.get("notas_fiscais"));
            String caminho = String.format("notas_fiscais/nota_fiscal_venda_%06d.txt", vendaId);
            Files.writeString(Paths.get(caminho), textoNota);
        } catch (IOException e) {
            System.err.println("Aviso: Não foi possível salvar o arquivo da nota fiscal: " + e.getMessage());
        }
    }

    private void exibirDialogoNotaFiscal(String textoNotaFiscal) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Emissão de Nota Fiscal - Venda Concluída!");

        TextArea textArea = new TextArea(textoNotaFiscal);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        textArea.setPrefWidth(650);
        textArea.setPrefHeight(520);

        Button btnCopiar = new Button("📋 Copiar Texto");
        btnCopiar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnCopiar.setOnAction(e -> {
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(textoNotaFiscal);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
            btnCopiar.setText("✓ Copiado!");
        });

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(12, btnCopiar, btnFechar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(10, 0, 0, 0));

        VBox raiz = new VBox(10, textArea, botoes);
        raiz.setPadding(new Insets(15));
        raiz.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(raiz);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
