package model.Entities;
import java.time.LocalDate;

public abstract class Pagamento {

    private int pagamento_id;
    private int venda_id;
    private double valor;
    private LocalDate data_pagamento;

    protected Pagamento(double valor) {
        setValor(valor);
        this.data_pagamento = LocalDate.now();
    }

    public boolean processar(){
        return true;
    }

    public double calcularValorFinal() {
        return valor;
    }

    // Getters e Setters

    public int getPagamento_id() {
        return pagamento_id;
    }

    public void setPagamento_id(int pagamento_id) {
        this.pagamento_id = pagamento_id;
    }

    public int getVenda_id() {
        return venda_id;
    }

    public void setVenda_id(int venda_id) {
        this.venda_id = venda_id;
    }

    public void setValor(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getData_pagamento() {
        return data_pagamento;
    }

    public void setData_pagamento(LocalDate data_pagamento) {
        this.data_pagamento = data_pagamento;
    }

}