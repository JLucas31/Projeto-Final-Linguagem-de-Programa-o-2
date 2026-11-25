package com.projeto_final_lp2.model.Entities;

// Estados de uma cotacao. No banco fica gravado o nome do enum
// (PENDENTE ou COMPRADO); na tela aparece o rotulo.
public enum StatusCotacao {

    PENDENTE("Pendente"),
    COMPRADO("Comprado");

    private final String rotulo;

    StatusCotacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
