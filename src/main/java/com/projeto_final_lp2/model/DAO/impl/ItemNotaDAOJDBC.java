package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.ItemNotaDAO;
import com.projeto_final_lp2.model.Entities.ItemNota;
import com.projeto_final_lp2.model.Entities.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de consulta de ItemNota (tabela item_nota).
 *
 * Os itens sao gravados pelo NotaFiscalCompraDAOJDBC, junto com a nota e
 * com o aumento de estoque.
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class ItemNotaDAOJDBC implements ItemNotaDAO {

    private static final String SELECT_BASE =
            "SELECT i.item_nota_id, i.qtd, i.valor_unitario, i.lote, " +
                    "p.produto_id, p.nomeProduto, p.marca, p.valor, p.quantidade, " +
                    "p.ultima_data_compra, p.setor " +
                    "FROM item_nota i " +
                    "JOIN produto p ON p.produto_id = i.produto_id ";

    // Retorna null se nao existir
    @Override
    public ItemNota buscarPorID(int itemNotaID) {

        List<ItemNota> itens = listar(
                SELECT_BASE + "WHERE i.item_nota_id = ?", itemNotaID);

        return itens.isEmpty() ? null : itens.get(0);
    }

    // Itens de uma nota
    @Override
    public List<ItemNota> listarPorNota(int notaID) {

        return listar(
                SELECT_BASE + "WHERE i.nota_id = ? ORDER BY i.item_nota_id",
                notaID);
    }

    // Entradas do produto, da mais recente para a mais antiga
    @Override
    public List<ItemNota> listarPorProduto(int produtoID) {

        return listar(
                SELECT_BASE + "WHERE i.produto_id = ? ORDER BY i.item_nota_id DESC",
                produtoID);
    }

    // Itens de um lote
    @Override
    public List<ItemNota> listarPorLote(String lote) {

        if (lote == null || lote.isBlank()) {
            throw new IllegalArgumentException("Informe o lote");
        }

        return listar(
                SELECT_BASE + "WHERE i.lote = ? ORDER BY i.item_nota_id",
                lote.trim());
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private List<ItemNota> listar(String sql, Object parametro) {

        List<ItemNota> itens = new ArrayList<>();

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(criarItem(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar itens de nota: " + e.getMessage(), e);
        }

        return itens;
    }

    private ItemNota criarItem(ResultSet rs) throws SQLException {

        Produto produto = ProdutoDAOJDBC.criarProduto(rs);

        ItemNota item = new ItemNota(
                produto,
                rs.getInt("qtd"),
                rs.getDouble("valor_unitario"),
                rs.getString("lote"));

        item.setItem_nota_id(rs.getInt("item_nota_id"));

        return item;
    }
}
