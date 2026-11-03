package model.Entities;

import java.time.LocalDate;

public class Venda {

    private int venda_id;
    private Produto produto;
    private Cliente cliente;
    private Vendedor vendedor;
    private LocalDate data_venda;
    private double valor_total;

    public Venda(int venda_id, Produto produto, Cliente cliente, Vendedor vendedor, LocalDate data_venda, double valor_total){
        this.venda_id = venda_id;
        this.produto = produto;
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.data_venda = data_venda;
        this.valor_total = valor_total;
    }

    public int getVenda_id() {
        return venda_id;
    }

    public void setVenda_id(int venda_id) {
        this.venda_id = venda_id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
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

    public void setValor_total(double valor_total) {
        this.valor_total = valor_total;
    }
}
