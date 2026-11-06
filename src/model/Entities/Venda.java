package model.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venda {

    private int venda_id;
    private final ArrayList<ItemVenda> itens = new ArrayList<>();
    private Cliente cliente;
    private Vendedor vendedor;
    private LocalDate data_venda;
    private double valor_total;
    private boolean finalizada;

    public Venda(int venda_id, Cliente cliente, Vendedor vendedor, LocalDate data_venda) {
        this.venda_id = venda_id;
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.data_venda = data_venda;
        this.valor_total = 0; // calculado a partir dos itens
        this.finalizada = false;
    }

    // ---------- Itens da venda ----------

    // Insere "qtd" unidades do produto. Se o produto ja esta na venda,
    // soma a quantidade no item existente (nao cria outra linha).
    public void inserirItem(Produto produto, int qtd) {
        verificarVendaAberta();
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        ItemVenda existente = buscarItem(produto.getProduto_id());
        int qtdTotal = qtd + (existente != null ? existente.getQtd() : 0);

        if (produto.getQuantidade() < qtdTotal) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        if (existente != null) {
            existente.setQtd(qtdTotal);
        } else {
            itens.add(new ItemVenda(produto, qtd));
        }
        calcularTotal();
    }

    // Remove apenas "qtd" unidades. Se pedir igual ou mais do que existe,
    // a linha inteira sai da venda.
    public void removerItem(int produto_id, int qtd) {
        verificarVendaAberta();
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        ItemVenda item = buscarItem(produto_id);
        if (item == null) {
            throw new IllegalArgumentException("Produto nao esta na venda");
        }

        if (qtd >= item.getQtd()) {
            itens.remove(item);
        } else {
            item.setQtd(item.getQtd() - qtd);
        }
        calcularTotal();
    }

    // Remove o produto por completo, qualquer que seja a quantidade
    public void removerItem(int produto_id) {
        verificarVendaAberta();

        ItemVenda item = buscarItem(produto_id);
        if (item == null) {
            throw new IllegalArgumentException("Produto nao esta na venda");
        }
        itens.remove(item);
        calcularTotal();
    }

    public void calcularTotal() {
        double soma = 0;
        for (ItemVenda item : itens) {
            soma += item.calculaSubtotal();
        }
        this.valor_total = soma;
    }

    private ItemVenda buscarItem(int produto_id) {
        for (ItemVenda item : itens) {
            if (item.getProduto().getProduto_id() == produto_id) {
                return item;
            }
        }
        return null;
    }

    private void verificarVendaAberta() {
        if (finalizada) {
            throw new IllegalStateException("Venda ja finalizada: nao pode ser alterada");
        }
    }

    // ---------- Finalizacao ----------

    // Confere o estoque de TODOS os itens antes de baixar qualquer um,
    // para nao baixar metade e falhar no meio.
    public void finalizarVenda() {
        verificarVendaAberta();
        if (itens.isEmpty()) {
            throw new IllegalStateException("Venda sem itens");
        }

        for (ItemVenda item : itens) {
            if (item.getProduto().getQuantidade() < item.getQtd()) {
                throw new IllegalStateException(
                        "Estoque insuficiente para o produto " + item.getProduto().getProduto_id());
            }
        }

        for (ItemVenda item : itens) {
            item.getProduto().baixarEstoque(item.getQtd());
        }
        this.finalizada = true;
    }

    // ---------- Getters e setters ----------

    public int getVenda_id() {
        return venda_id;
    }

    public void setVenda_id(int venda_id) {
        this.venda_id = venda_id;
    }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public LocalDate getData_venda() {
        return data_venda;
    }

    public void setData_venda(LocalDate data_venda) {
        this.data_venda = data_venda;
    }

    public double getValor_total() {
        return valor_total;
    }

    // Sem setValor_total: o total eh sempre calculado a partir dos itens.

    public boolean isFinalizada() {
        return finalizada;
    }
}