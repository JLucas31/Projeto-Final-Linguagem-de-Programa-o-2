package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.ItemVendaDAO;
import com.projeto_final_lp2.model.Entities.ItemVenda;
import com.projeto_final_lp2.model.Entities.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de consulta de ItemVenda (tabela item_venda).
 *
 * Os itens sao gravados pelo VendaDAOJDBC, junto com a venda.
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class ItemVendaDAOJDBC implements ItemVendaDAO {

    // Item + dados do produto (o produto atual, nao o preco da venda:
    // o preco da venda vem de i.valor_unitario)
    private static final String SELECT_BASE =
            "SELECT i.item_venda_id, i.qtd, i.valor_unitario, " +
                    "p.produto_id, p.nomeProduto, p.marca, p.valor, p.quantidade, " +
                    "p.ultima_data_compra, p.setor, p.quantidade_minima " +
                    "FROM item_venda i " +
                    "JOIN produto p ON p.produto_id = i.produto_id ";

    // Retorna null se nao existir
    @Override
    public ItemVenda buscarPorID(int itemVendaID) {

        List<ItemVenda> itens = listar(
                SELECT_BASE + "WHERE i.item_venda_id = ?", itemVendaID);

        return itens.isEmpty() ? null : itens.get(0);
    }

    // Itens de uma venda
    @Override
    public List<ItemVenda> listarPorVenda(int vendaID) {

        return listar(
                SELECT_BASE + "WHERE i.venda_id = ? ORDER BY i.item_venda_id",
                vendaID);
    }

    // Historico do produto: as linhas de venda mais recentes primeiro
    @Override
    public List<ItemVenda> listarPorProduto(int produtoID) {

        return listar(
                SELECT_BASE + "WHERE i.produto_id = ? ORDER BY i.item_venda_id DESC",
                produtoID);
    }

    // So conta vendas finalizadas (venda aberta ainda nao baixou o estoque)
    @Override
    public int totalVendidoDoProduto(int produtoID) {

        String sql = "SELECT COALESCE(SUM(i.qtd), 0) " +
                "FROM item_venda i " +
                "JOIN venda v ON v.venda_id = i.venda_id " +
                "WHERE i.produto_id = ? AND v.finalizada = TRUE";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoID);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao somar vendas do produto: " + e.getMessage(), e);
        }
    }

    // Auxiliares

    private List<ItemVenda> listar(String sql, int parametro) {

        List<ItemVenda> itens = new ArrayList<>();

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(criarItem(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar itens de venda: " + e.getMessage(), e);
        }

        return itens;
    }

    private ItemVenda criarItem(ResultSet rs) throws SQLException {

        Produto produto = ProdutoDAOJDBC.criarProduto(rs);

        // valor_unitario eh o preco NA HORA da venda,
        // nao o preco atual do produto
        ItemVenda item = new ItemVenda(
                produto,
                rs.getInt("qtd"),
                rs.getDouble("valor_unitario"));

        item.setItem_venda_id(rs.getInt("item_venda_id"));

        return item;
    }
}
