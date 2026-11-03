package model.Entities;

public class Produto {

    private int produto_id;
    private String nomeProduto;
    private String marca;
    private double preco;
    private int quantidade;
    private String lote;

    public Produto(int produto_id,  String nomeProduto, String marca, double preco, int quantidade, String lote) {
        this.produto_id = produto_id;
        this.nomeProduto = nomeProduto;
        this.marca = marca;
        this.preco = preco;
        this.quantidade = quantidade;
        this.lote = lote;
    }

    public int getProduto_id() {
        return produto_id;
    }

    public void setProduto_id(int produto_id) {
        this.produto_id = produto_id;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }
}
