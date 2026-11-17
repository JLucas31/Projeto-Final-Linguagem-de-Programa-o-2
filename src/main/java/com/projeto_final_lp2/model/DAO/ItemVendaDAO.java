package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.ItemVenda;
import java.util.List;

// DAO de CONSULTA dos itens de venda.
//
// Gravar, alterar e remover itens eh feito pelo VendaDAO, junto com a
// venda: o item nao existe sozinho, e mexer nele sem atualizar o
// valor_total e o estoque deixaria o banco inconsistente.
public interface ItemVendaDAO {

    // Retorna null se nao existir
    ItemVenda buscarPorID(int itemVendaID);

    // Itens de uma venda, na ordem em que foram inseridos
    List<ItemVenda> listarPorVenda(int vendaID);

    // Todas as linhas de venda em que o produto aparece (historico do produto)
    List<ItemVenda> listarPorProduto(int produtoID);

    // Quantidade total vendida do produto, considerando so vendas finalizadas
    int totalVendidoDoProduto(int produtoID);
}
