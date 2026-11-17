package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.Pagamento;

// Um unico DAO cuida de Pagamento e de todas as suas subclasses
// (Dinheiro, Pix, Debito e Credito): ele grava/le a tabela pagamento
// e a tabela da subclasse correspondente.

public interface PagamentoDAO {

    // O pagamento precisa ter o venda_id preenchido (setVenda_id)
    void cadastrarPagamento(Pagamento pagamento);

    Pagamento buscarPorID(int id);

    Pagamento buscarPorVenda(int vendaID);

    void excluirPorVenda(int vendaID);
}
