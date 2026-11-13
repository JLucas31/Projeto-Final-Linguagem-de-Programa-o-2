package model.Entities;

public class PagamentoDinheiro extends Pagamento {

    private double valor_recebido;

    public PagamentoDinheiro(double valor, double valor_recebido) {
        super(valor);
        setValor_recebido(valor_recebido);
    }

    // Aceita o pagamento se o cliente entregou pelo menos o valor da venda

    @Override
    public boolean processar() {
        return valor_recebido >= getValor();
    }

    public double calcularTroco() {
        if (!processar()) {
            throw new IllegalStateException("Valor recebido insuficiente");
        }
        return valor_recebido - getValor();
    }

    public double getValor_recebido() {
        return valor_recebido;
    }

    public void setValor_recebido(double valor_recebido) {
        if (valor_recebido <= 0) {
            throw new IllegalArgumentException("Valor recebido deve ser maior que zero");
        }
        this.valor_recebido = valor_recebido;
    }
}