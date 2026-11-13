package model.Entities;

public class PagamentoDebito extends Pagamento {

    private double valor_recebido;

    public PagamentoDebito(double valor, double valor_recebido) {
        super(valor);
        setValor_recebido(valor_recebido);
    }

    @Override
    public boolean processar() {
        return valor_recebido >= getValor();
    }

    public double getValor_recebido() {
        return valor_recebido;
    }

    public void setValor_recebido(double valor_recebido) {
        this.valor_recebido = valor_recebido;
    }
}
