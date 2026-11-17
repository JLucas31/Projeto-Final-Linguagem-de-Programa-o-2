package com.projeto_final_lp2.model.Entities;

public class PagamentoDebito extends Pagamento {

    private String bandeira;
    private String ultimos_digitos;

    public PagamentoDebito(double valor, String bandeira, String ultimos_digitos) {
        super(valor);
        setBandeira(bandeira);
        setUltimos_digitos(ultimos_digitos);
    }

    // processar() eh herdado de Pagamento (retorna true): os dados do
    // cartao ja sao validados nos setters.

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        if (bandeira == null || bandeira.isBlank()) {
            throw new IllegalArgumentException("Bandeira invalida");
        }
        this.bandeira = bandeira;
    }

    public String getUltimos_digitos() {
        return ultimos_digitos;
    }

    public void setUltimos_digitos(String ultimos_digitos) {
        if (ultimos_digitos == null || !ultimos_digitos.matches("\\d{4}")) {
            throw new IllegalArgumentException("Informe os 4 ultimos digitos do cartao");
        }
        this.ultimos_digitos = ultimos_digitos;
    }
}