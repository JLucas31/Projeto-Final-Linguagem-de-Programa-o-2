package com.projeto_final_lp2.model.Entities;

public class PagamentoPix extends Pagamento {

    private String chave_pix;
    private String id_transacao;

    // Construtor principal (valor e chave Pix)
    public PagamentoPix(double valor, String chave_pix) {
        super(valor);
        setChave_pix(chave_pix);
    }

    // Construtor completo (valor, chave Pix e ID da transação)
    public PagamentoPix(double valor, String chave_pix, String id_transacao) {
        super(valor);
        setChave_pix(chave_pix);
        setId_transacao(id_transacao);
    }

    // Construtor mantido para compatibilidade com código legado
    public PagamentoPix(double valor, double valor_recebido) {
        super(valor);
        this.chave_pix = "CHAVE_NAO_INFORMADA";
    }

    // O Pix é aceito se a chave Pix estiver preenchida corretamente
    @Override
    public boolean processar() {
        return chave_pix != null && !chave_pix.isBlank();
    }

    // Getters e Setters

    public String getChave_pix() {
        return chave_pix;
    }

    public void setChave_pix(String chave_pix) {
        if (chave_pix == null || chave_pix.isBlank()) {
            throw new IllegalArgumentException("Chave Pix não pode ser nula ou vazia");
        }
        if (chave_pix.length() > 77) {
            throw new IllegalArgumentException("Chave Pix excede o tamanho máximo de 77 caracteres");
        }
        this.chave_pix = chave_pix;
    }

    public String getId_transacao() {
        return id_transacao;
    }

    public void setId_transacao(String id_transacao) {
        if (id_transacao != null && id_transacao.length() > 50) {
            throw new IllegalArgumentException("ID da transação excede o tamanho máximo de 50 caracteres");
        }
        this.id_transacao = id_transacao;
    }

    // Métodos mantidos para compatibilidade legado (evitar quebra em VendaDAOJDBC)
    public double getValor_recebido() {
        return getValor();
    }

    public void setValor_recebido(double valor_recebido) {
        // Sem efeito para Pix
    }
}
