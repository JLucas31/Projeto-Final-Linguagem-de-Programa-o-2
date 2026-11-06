package model.Entities;

public class ItemVenda {

    private int item_venda_id;
    private Produto produto;
    private int qtd;
    private double valor_unitario; // preco do produto NA HORA da venda

    // Usa o preco atual do produto como valor unitario
    public ItemVenda(Produto produto, int qtd) {
        this(produto, qtd, produto.getValor());
    }

    public ItemVenda(Produto produto, int qtd, double valor_unitario) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto nao pode ser nulo");
        }
        this.produto = produto;
        setQtd(qtd);
        setValor_unitario(valor_unitario);
    }

    public double calculaSubtotal() {
        return qtd * valor_unitario;
    }

    public int getItem_venda_id() {
        return item_venda_id;
    }

    public void setItem_venda_id(int item_venda_id) {
        this.item_venda_id = item_venda_id;
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
}