package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.ItemNota;
import java.util.List;

// DAO de CONSULTA dos itens de nota fiscal de compra.
//
// Os itens sao gravados pelo NotaFiscalCompraDAO.registrarEntrada(), que
// tambem aumenta o estoque numa unica transacao. Gravar um item sozinho
// aqui deixaria o estoque diferente da nota.
public interface ItemNotaDAO {

    // Retorna null se nao existir
    ItemNota buscarPorID(int itemNotaID);

    // Itens de uma nota, na ordem em que foram inseridos
    List<ItemNota> listarPorNota(int notaID);

    // Todas as entradas do produto, da mais recente para a mais antiga
    List<ItemNota> listarPorProduto(int produtoID);

    // Itens de um lote especifico (um lote pode ter varios produtos)
    List<ItemNota> listarPorLote(String lote);
}
