package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.NotaFiscalCompraDAO;
import com.projeto_final_lp2.model.Entities.ItemNota;
import com.projeto_final_lp2.model.Entities.NotaFiscalCompra;
import com.projeto_final_lp2.model.Entities.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de NotaFiscalCompra. Os itens (item_nota) sao gravados e lidos junto
 * com a nota. Registrar uma nota mexe em 3 tabelas (nota_fiscal_compra,
 * item_nota e produto), entao roda numa transacao: ou grava tudo, ou nada.
 *
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class NotaFiscalCompraDAOJDBC implements NotaFiscalCompraDAO {

    // Registra a entrada: grava a nota e os itens e, para cada item, soma a
    // quantidade ao estoque do produto e atualiza a ultima data de compra.
    // (Os objetos Produto que ja estao em memoria nao sao alterados: busque
    // o produto de novo no ProdutoDAO para ver o estoque novo.)
    @Override
    public void registrarEntrada(NotaFiscalCompra nota) {

        validarNota(nota);

        int idOriginal = nota.getNota_id();

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            exigirNotaNova(conn, nota);
            inserirNota(conn, nota);
            inserirItensEAtualizarEstoque(conn, nota);

            conn.commit();
            System.out.println("Entrada de mercadoria registrada com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            desfazerIdsEmMemoria(nota, idOriginal);
            throw new RuntimeException(
                    "Erro ao registrar nota fiscal: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            desfazerIdsEmMemoria(nota, idOriginal);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // Retorna null se nao existir
    @Override
    public NotaFiscalCompra buscarPorID(int id) {

        Connection conn = DataBase.getConnection();

        try {
            return carregarNota(conn, id);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar nota fiscal: " + e.getMessage(), e);
        }
    }

    // Retorna null se nao existir
    @Override
    public NotaFiscalCompra buscarPorNumero(String numero) {

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT nota_id FROM nota_fiscal_compra WHERE numero = ?")) {

            stmt.setString(1, numero);

            int id;

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }
                id = rs.getInt("nota_id");
            }

            return carregarNota(conn, id);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar nota fiscal: " + e.getMessage(), e);
        }
    }

    // Notas com data de entrada entre inicio e fim (inclusive), da mais
    // antiga para a mais recente
    @Override
    public List<NotaFiscalCompra> listarPorPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser depois da data final");
        }

        Connection conn = DataBase.getConnection();

        String sql = "SELECT nota_id FROM nota_fiscal_compra " +
                "WHERE data_entrada BETWEEN ? AND ? " +
                "ORDER BY data_entrada, nota_id";

        try {
            // Primeiro le so os IDs, depois monta cada nota completa
            List<Integer> ids = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, java.sql.Date.valueOf(inicio));
                stmt.setDate(2, java.sql.Date.valueOf(fim));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt("nota_id"));
                    }
                }
            }

            List<NotaFiscalCompra> notas = new ArrayList<>();

            for (int id : ids) {
                NotaFiscalCompra nota = carregarNota(conn, id);
                if (nota != null) {
                    notas.add(nota);
                }
            }

            return notas;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar notas fiscais: " + e.getMessage(), e);
        }
    }

    // O banco nao impede numero ou chave repetidos, entao conferimos aqui
    private void exigirNotaNova(Connection conn, NotaFiscalCompra nota) throws SQLException {

        String sql = "SELECT COUNT(*) FROM nota_fiscal_compra " +
                "WHERE numero = ? OR chave_acesso = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nota.getNumero());
            stmt.setString(2, nota.getChave_acesso());

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new IllegalStateException(
                            "Já existe uma nota com este número ou esta chave de acesso");
                }
            }
        }
    }

    private void inserirNota(Connection conn, NotaFiscalCompra nota) throws SQLException {

        nota.calcularTotal();

        String sql = "INSERT INTO nota_fiscal_compra " +
                "(numero, chave_acesso, fornecedor, data_entrada, valor_total) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nota.getNumero());
            stmt.setString(2, nota.getChave_acesso());
            stmt.setString(3, nota.getFornecedor());
            stmt.setDate(4, java.sql.Date.valueOf(nota.getData_entrada()));
            stmt.setDouble(5, nota.getValor_total());

            stmt.executeUpdate();

            // Recupera o ID gerado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    nota.setNota_id(rs.getInt(1));
                }
            }
        }
    }

    private void inserirItensEAtualizarEstoque(Connection conn, NotaFiscalCompra nota)
            throws SQLException {

        String sqlEstoque = "UPDATE produto " +
                "SET quantidade = quantidade + ?, ultima_data_compra = ? " +
                "WHERE produto_id = ?";

        String sqlItem = "INSERT INTO item_nota " +
                "(nota_id, produto_id, qtd, valor_unitario, lote) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement estoque = conn.prepareStatement(sqlEstoque);
             PreparedStatement item =
                     conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {

            for (ItemNota itemNota : nota.getItens()) {

                int produtoID = itemNota.getProduto().getProduto_id();

                // Estoque primeiro: se o produto nao existe, o erro fica claro
                estoque.setInt(1, itemNota.getQtd());
                estoque.setDate(2, java.sql.Date.valueOf(nota.getData_entrada()));
                estoque.setInt(3, produtoID);

                if (estoque.executeUpdate() == 0) {
                    throw new IllegalArgumentException(
                            "Produto não encontrado: " + produtoID);
                }

                item.setInt(1, nota.getNota_id());
                item.setInt(2, produtoID);
                item.setInt(3, itemNota.getQtd());
                item.setDouble(4, itemNota.getValor_unitario());
                item.setString(5, itemNota.getLote());

                item.executeUpdate();

                try (ResultSet rs = item.getGeneratedKeys()) {
                    if (rs.next()) {
                        itemNota.setItem_nota_id(rs.getInt(1));
                    }
                }
            }
        }
    }

    // =====================================================================
    // Leitura
    // =====================================================================

    // Monta a nota completa (com itens e produtos). Retorna null se nao existir.
    private NotaFiscalCompra carregarNota(Connection conn, int notaID) throws SQLException {

        String sql = "SELECT numero, chave_acesso, fornecedor, data_entrada " +
                "FROM nota_fiscal_compra WHERE nota_id = ?";

        NotaFiscalCompra nota;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notaID);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                nota = new NotaFiscalCompra(
                        notaID,
                        rs.getString("numero"),
                        rs.getString("chave_acesso"),
                        rs.getString("fornecedor"),
                        rs.getDate("data_entrada").toLocalDate());
            }
        }

        carregarItens(conn, nota);

        return nota;
    }

    private void carregarItens(Connection conn, NotaFiscalCompra nota) throws SQLException {

        String sql = "SELECT i.item_nota_id, i.qtd, i.valor_unitario, i.lote, " +
                "p.produto_id, p.nomeProduto, p.marca, p.valor, p.quantidade, " +
                "p.ultima_data_compra, p.setor, p.quantidade_minima " +
                "FROM item_nota i " +
                "JOIN produto p ON p.produto_id = i.produto_id " +
                "WHERE i.nota_id = ? " +
                "ORDER BY i.item_nota_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nota.getNota_id());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Produto produto = ProdutoDAOJDBC.criarProduto(rs);

                    ItemNota item = new ItemNota(
                            produto,
                            rs.getInt("qtd"),
                            rs.getDouble("valor_unitario"),
                            rs.getString("lote"));

                    item.setItem_nota_id(rs.getInt("item_nota_id"));

                    nota.adicionarItem(item);
                }
            }
        }
    }

    // =====================================================================
    // Validacao e controle de transacao
    // =====================================================================

    private void validarNota(NotaFiscalCompra nota) {

        if (nota == null) {
            throw new IllegalArgumentException("Nota fiscal não pode ser nula");
        }
        if (nota.getItens().isEmpty()) {
            throw new IllegalArgumentException("A nota fiscal precisa ter ao menos um item");
        }
        for (ItemNota item : nota.getItens()) {
            if (item.getProduto().getProduto_id() <= 0) {
                throw new IllegalArgumentException(
                        "Todos os produtos da nota precisam estar cadastrados");
            }
        }
    }

    // Se a transacao falhou, os IDs gerados em memoria nao existem no banco
    private void desfazerIdsEmMemoria(NotaFiscalCompra nota, int idOriginal) {

        nota.setNota_id(idOriginal);

        for (ItemNota item : nota.getItens()) {
            item.setItem_nota_id(0);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignorada) {
            // se o rollback falhar, a excecao original eh a que importa
        }
    }

    private void restaurarAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // conexao ja pode ter sido fechada
        }
    }
}
