package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.CotacaoDAO;
import com.projeto_final_lp2.model.Entities.Cotacao;
import com.projeto_final_lp2.model.Entities.Produto;
import com.projeto_final_lp2.model.Entities.StatusCotacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Cotacao (tabela cotacao).
 *
 * O SELECT faz JOIN com produto para montar o Produto de cada cotacao com
 * o mesmo metodo usado pelo ProdutoDAOJDBC.
 *
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class CotacaoDAOJDBC implements CotacaoDAO {

    private static final String SELECT_BASE =
            "SELECT c.id_cotacao, c.data_cotacao, c.quantidade_cotacao, c.status, " +
                    "p.produto_id, p.nomeProduto, p.marca, p.valor, p.quantidade, " +
                    "p.ultima_data_compra, p.setor, p.quantidade_minima " +
                    "FROM cotacao c " +
                    "JOIN produto p ON p.produto_id = c.produto_id ";

    // Cria a cotacao (quantidade 0, pendente) ou devolve a pendente que ja existe
    @Override
    public Cotacao solicitarCotacao(int produtoId) {

        Connection conn = DataBase.getConnection();

        try {
            Cotacao pendente = buscarPendenteDoProduto(conn, produtoId);
            if (pendente != null) {
                return pendente;
            }

            String sql = "INSERT INTO cotacao " +
                    "(data_cotacao, produto_id, quantidade_cotacao, status) " +
                    "VALUES (?, ?, 0, ?)";

            int idGerado = 0;

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setInt(2, produtoId);
                stmt.setString(3, StatusCotacao.PENDENTE.name());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGerado = rs.getInt(1);
                    }
                }
            }

            System.out.println("Produto colocado para cotação!");
            return buscarPorID(idGerado);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao colocar produto em cotação: " + e.getMessage(), e);
        }
    }

    // Retorna null se nao existir
    @Override
    public Cotacao buscarPorID(int id) {

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt =
                     conn.prepareStatement(SELECT_BASE + "WHERE c.id_cotacao = ?")) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarCotacao(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar cotação: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public List<Cotacao> listarCotacoes() {

        List<Cotacao> cotacoes = new ArrayList<>();

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                SELECT_BASE + "ORDER BY c.data_cotacao DESC, c.id_cotacao DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cotacoes.add(criarCotacao(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar cotações: " + e.getMessage(), e);
        }

        return cotacoes;
    }

    // A quantidade so muda enquanto a cotacao esta pendente (o "AND status"
    // faz a checagem e a alteracao no mesmo UPDATE)
    @Override
    public void definirQuantidade(int idCotacao, int quantidade) {

        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa");
        }

        String sql = "UPDATE cotacao SET quantidade_cotacao = ? " +
                "WHERE id_cotacao = ? AND status = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantidade);
            stmt.setInt(2, idCotacao);
            stmt.setString(3, StatusCotacao.PENDENTE.name());

            if (stmt.executeUpdate() == 0) {
                exigirExistente(idCotacao);
                throw new IllegalStateException(
                        "Esta cotação já foi comprada: volte-a para pendente para alterar a quantidade");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao definir quantidade da cotação: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizarStatus(int idCotacao, StatusCotacao status) {

        if (status == null) {
            throw new IllegalArgumentException("Informe o status");
        }

        Cotacao cotacao = exigirExistente(idCotacao);

        if (status == StatusCotacao.COMPRADO && cotacao.getQuantidade_cotacao() <= 0) {
            throw new IllegalStateException(
                    "Defina a quantidade a comprar antes de marcar como comprado");
        }

        String sql = "UPDATE cotacao SET status = ? WHERE id_cotacao = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, idCotacao);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar status da cotação: " + e.getMessage(), e);
        }
    }

    @Override
    public void removerCotacao(int idCotacao) {

        String sql = "DELETE FROM cotacao WHERE id_cotacao = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCotacao);

            if (stmt.executeUpdate() == 0) {
                throw new IllegalArgumentException("Cotação não encontrada: " + idCotacao);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao remover cotação: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private Cotacao buscarPendenteDoProduto(Connection conn, int produtoId) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                SELECT_BASE + "WHERE c.produto_id = ? AND c.status = ? " +
                        "ORDER BY c.id_cotacao DESC LIMIT 1")) {

            stmt.setInt(1, produtoId);
            stmt.setString(2, StatusCotacao.PENDENTE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarCotacao(rs);
                }
            }
        }

        return null;
    }

    private Cotacao exigirExistente(int idCotacao) {
        Cotacao cotacao = buscarPorID(idCotacao);
        if (cotacao == null) {
            throw new IllegalArgumentException("Cotação não encontrada: " + idCotacao);
        }
        return cotacao;
    }

    private Cotacao criarCotacao(ResultSet rs) throws SQLException {

        Produto produto = ProdutoDAOJDBC.criarProduto(rs);

        return new Cotacao(
                rs.getInt("id_cotacao"),
                rs.getDate("data_cotacao").toLocalDate(),
                produto,
                rs.getInt("quantidade_cotacao"),
                StatusCotacao.valueOf(rs.getString("status")));
    }
}
