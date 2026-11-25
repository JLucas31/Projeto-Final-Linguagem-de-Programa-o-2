package com.projeto_final_lp2.model.Entities;

import java.time.LocalDate;

// Cotacao de um produto que chegou na quantidade minima do estoque.
//
// Nasce com quantidade_cotacao = 0 e status PENDENTE. A quantidade a comprar
// so eh definida depois, na aba Cotacao.
//
// Os getters seguem o padrao do projeto (getVenda_id, getData_venda...) para
// funcionarem direto com PropertyValueFactory nas colunas da tabela.
public class Cotacao {

    private int id_cotacao;
    private LocalDate data_cotacao;
    private final Produto produto;
    private int quantidade_cotacao;
    private StatusCotacao status;

    // Cotacao nova: data de hoje, quantidade 0 e pendente
    public Cotacao(Produto produto) {
        this(0, LocalDate.now(), produto, 0, StatusCotacao.PENDENTE);
    }

    public Cotacao(int id_cotacao, LocalDate data_cotacao, Produto produto,
                   int quantidade_cotacao, StatusCotacao status) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto nao pode ser nulo");
        }
        this.id_cotacao = id_cotacao;
        this.produto = produto;
        setData_cotacao(data_cotacao);
        setQuantidade_cotacao(quantidade_cotacao);
        setStatus(status);
    }

    public int getId_cotacao() {
        return id_cotacao;
    }

    public void setId_cotacao(int id_cotacao) {
        this.id_cotacao = id_cotacao;
    }

    public LocalDate getData_cotacao() {
        return data_cotacao;
    }

    public void setData_cotacao(LocalDate data_cotacao) {
        if (data_cotacao == null) {
            throw new IllegalArgumentException("Data da cotacao invalida");
        }
        this.data_cotacao = data_cotacao;
    }

    public Produto getProduto() {
        return produto;
    }

    // Atalhos para as colunas da tabela da aba Cotacao
    public int getProduto_id() {
        return produto.getProduto_id();
    }

    public String getNomeProduto() {
        return produto.getNomeProduto();
    }

    public int getQuantidade_cotacao() {
        return quantidade_cotacao;
    }

    public void setQuantidade_cotacao(int quantidade_cotacao) {
        if (quantidade_cotacao < 0) {
            throw new IllegalArgumentException("A quantidade da cotacao nao pode ser negativa");
        }
        this.quantidade_cotacao = quantidade_cotacao;
    }

    public StatusCotacao getStatus() {
        return status;
    }

    public void setStatus(StatusCotacao status) {
        if (status == null) {
            throw new IllegalArgumentException("Status da cotacao invalido");
        }
        this.status = status;
    }

    // Texto do status para a coluna da tabela ("Pendente" / "Comprado")
    public String getStatusTexto() {
        return status.getRotulo();
    }
}
