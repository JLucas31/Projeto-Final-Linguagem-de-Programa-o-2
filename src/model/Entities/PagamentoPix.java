package model.Entities;

public class PagamentoPix extends Pagamento{

    private double valor_recebido;

    public PagamentoPix(double valor, double valor_recebido) {
        super(valor);
        setValor_recebido(valor_recebido);
    }

    @Override
    public boolean processar() {
        return false;
    }

    // Getters e Setters

    public double getValor_recebido() {
        return valor_recebido;
    }

    public void setValor_recebido(double valor_recebido) {
        this.valor_recebido = valor_recebido;
    }

}
