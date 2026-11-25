package com.projeto_final_lp2.model.Entities;

public class ItemNota {

    private int item_nota_id;
    private final Produto produto;
    private int qtd;
    private double valor_unitario; // preco de custo do produto nesta compra
    private String lote;           // o lote fica na entrada de mercadoria

    public ItemNota(Produto produto, int qtd, double valor_unitario, String lote) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto nao pode ser nulo");
        }
        this.produto = produto;
        setQtd(qtd);
        setValor_unitario(valor_unitario);
        this.lote = lote;
    }

    public double calculaSubtotal() {
        return qtd * valor_unitario;
    }

    public int getItem_nota_id() {
        return item_nota_id;
    }

    public void setItem_nota_id(int item_nota_id) {
        this.item_nota_id = item_nota_id;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.qtd = qtd;
    }

    public double getValor_unitario() {
        return valor_unitario;
    }

    public void setValor_unitario(double valor_unitario) {
        if (valor_unitario < 0) {
            throw new IllegalArgumentException("Valor unitario nao pode ser negativo");
        }
        this.valor_unitario = valor_unitario;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }
}