package com.projeto_final_lp2.controller;

import com.projeto_final_lp2.model.DAO.VendaDAO;
import com.projeto_final_lp2.model.DAO.impl.VendaDAOJDBC;
import com.projeto_final_lp2.model.Entities.ItemVenda;
import com.projeto_final_lp2.model.Entities.Pagamento;
import com.projeto_final_lp2.model.Entities.Venda;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

// Aba Vendas: tabela de vendas e acesso ao ponto de venda (nova venda).
public class VendasController implements Initializable {

    @FXML
    private TableView<Venda> tabela;
    @FXML
    private TableColumn<Venda, Integer> colId;
    @FXML
    private TableColumn<Venda, String> colCliente;
    @FXML
    private TableColumn<Venda, String> colVendedor;
    @FXML
    private TableColumn<Venda, LocalDate> colData;
    @FXML
    private TableColumn<Venda, Double> colTotal;
    @FXML
    private TableColumn<Venda, String> colStatus;
    @FXML
    private Label lblStatus;

    private final VendaDAO vendaDAO = new VendaDAOJDBC();
    private final ObservableList<Venda> lista = FXCollections.observableArrayList();
    private FiltroTabela<Venda> filtro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("venda_id"));
        colCliente.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
                d.getValue().getCliente() != null ? d.getValue().getCliente().getNomeCliente() : ""));
        colVendedor.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
                d.getValue().getVendedor() != null ? d.getValue().getVendedor().getNomeVendedor() : ""));
        colData.setCellValueFactory(new PropertyValueFactory<>("data_venda"));
        colData.setCellFactory(c -> Util.celulaData());
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valor_total"));
        colTotal.setCellFactory(c -> Util.celulaMoeda());
        colStatus.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(
                d.getValue().isFinalizada() ? "Finalizada" : "Em aberto"));

        // Campo de busca embaixo do nome de cada coluna (palavras separadas por '+')
        filtro = new FiltroTabela<>(tabela, lista)
                .coluna(colId, v -> String.valueOf(v.getVenda_id()))
                .coluna(colCliente, v -> v.getCliente() != null ? v.getCliente().getNomeCliente() : "")
                .coluna(colVendedor, v -> v.getVendedor() != null ? v.getVendedor().getNomeVendedor() : "")
                .coluna(colData, v -> Util.data(v.getData_venda()))
                .coluna(colTotal, v -> Util.moeda(v.getValor_total()))
                .coluna(colStatus, v -> v.isFinalizada() ? "Finalizada" : "Em aberto");

        carregar();
    }

    private void carregar() {
        try {
            List<Venda> vendas = vendaDAO.listarVendas();
            lista.setAll(vendas);
            lblStatus.setText(vendas.size() + " venda(s) registrada(s).");
        } catch (Exception e) {
            Util.erro("Falha ao carregar as vendas", e.getMessage());
        }
    }

    @FXML
    private void onVoltar(ActionEvent event) {
        Navegacao.irParaMenu((Node) event.getSource());
    }

    // Abre o ponto de venda (venda-view.fxml) e recarrega a tabela ao fechar
    @FXML
    private void onNovaVenda(ActionEvent event) {
        try {
            Parent raiz = FXMLLoader.load(Navegacao.localizar("venda-view.fxml"));

            Stage janela = new Stage();
            janela.initModality(Modality.WINDOW_MODAL);
            janela.initOwner(tabela.getScene().getWindow());
            janela.setTitle("Ponto de Venda (PDV) - Nova Venda");
            janela.setMinWidth(950);
            janela.setMinHeight(650);
            janela.setScene(new Scene(raiz, 1120, 760));
            janela.showAndWait();

            carregar();
        } catch (Exception e) {
            Util.erro("Não foi possível abrir a tela de venda", e.getMessage());
        }
    }

    @FXML
    private void onVerItens(ActionEvent event) {
        Venda selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Util.aviso("Nenhuma venda selecionada", "Selecione uma venda na tabela.");
            return;
        }

        StringBuilder texto = new StringBuilder();
        for (ItemVenda item : selecionada.getItens()) {
            texto.append(String.format("%dx %s - %s (subtotal %s)%n",
                    item.getQtd(),
                    item.getProduto().getNomeProduto(),
                    Util.moeda(item.getValor_unitario()),
                    Util.moeda(item.calculaSubtotal())));
        }

        Pagamento pagamento = selecionada.getPagamento();
        texto.append(String.format("%nTotal: %s", Util.moeda(selecionada.getValor_total())));
        if (pagamento != null) {
            texto.append("\nPagamento: ").append(pagamento.getClass().getSimpleName().replace("Pagamento", ""));
        }

        Util.info("Venda #" + selecionada.getVenda_id(), texto.toString());
    }

    @FXML
    private void onAtualizar(ActionEvent event) {
        filtro.limparFiltros();
        carregar();
    }
}
