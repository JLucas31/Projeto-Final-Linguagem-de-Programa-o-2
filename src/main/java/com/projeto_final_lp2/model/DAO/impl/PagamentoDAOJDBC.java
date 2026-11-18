package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.PagamentoDAO;
import com.projeto_final_lp2.model.Entities.Pagamento;
import com.projeto_final_lp2.model.Entities.PagamentoCredito;
import com.projeto_final_lp2.model.Entities.PagamentoDebito;
import com.projeto_final_lp2.model.Entities.PagamentoDinheiro;
import com.projeto_final_lp2.model.Entities.PagamentoPix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * DAO de Pagamento e de todas as subclasses. Cada pagamento ocupa DUAS
 * linhas: uma em "pagamento" (dados comuns + coluna tipo) e outra na
 * tabela da subclasse (pagamento_dinheiro, pagamento_pix, ...), com o
 * mesmo pagamento_id.
 *
 * Os metodos da interface abrem a propria transacao. Os metodos que
 * recebem Connection (usados pelo VendaDAOJDBC) rodam dentro da transacao
 * que o chamador ja abriu, sem fazer commit.
 *
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class PagamentoDAOJDBC implements PagamentoDAO {

    private static final String[] TABELAS_SUBCLASSES = {
            "pagamento_dinheiro", "pagamento_pix",
            "pagamento_debito", "pagamento_credito"
    };

    private static final String SQL_POR_ID =
            "SELECT pagamento_id, venda_id, tipo, valor, data_pagamento " +
                    "FROM pagamento WHERE pagamento_id = ?";

    private static final String SQL_POR_VENDA =
            "SELECT pagamento_id, venda_id, tipo, valor, data_pagamento " +
                    "FROM pagamento WHERE venda_id = ?";

    // =====================================================================
    // Operacoes da interface (cada uma com a sua transacao)
    // =====================================================================

    @Override
    public void cadastrarPagamento(Pagamento pagamento) {

        int idOriginal = (pagamento != null) ? pagamento.getPagamento_id() : 0;

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            cadastrarPagamento(conn, pagamento);

            conn.commit();
            System.out.println("Pagamento cadastrado com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            restaurarId(pagamento, idOriginal);
            throw new RuntimeException(
                    "Erro ao cadastrar pagamento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            restaurarId(pagamento, idOriginal);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    @Override
    public Pagamento buscarPorID(int id) {

        Connection conn = DataBase.getConnection();

        try {
            return carregar(conn, SQL_POR_ID, id);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public Pagamento buscarPorVenda(int vendaID) {

        Connection conn = DataBase.getConnection();

        try {
            return buscarPorVenda(conn, vendaID);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar pagamento da venda: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluirPorVenda(int vendaID) {

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            excluirPorVenda(conn, vendaID);

            conn.commit();
            System.out.println("Pagamento excluído com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(
                    "Erro ao excluir pagamento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // =====================================================================
    // Versoes que rodam dentro de uma transacao ja aberta (sem commit)
    // =====================================================================

    // Grava o pagamento. Como o banco nao impede dois pagamentos na mesma
    // venda, conferimos aqui.
    public void cadastrarPagamento(Connection conn, Pagamento pagamento) throws SQLException {

        if (pagamento == null) {
            throw new IllegalArgumentException("Pagamento não pode ser nulo");
        }
        if (pagamento.getVenda_id() <= 0) {
            throw new IllegalArgumentException(
                    "Informe a venda do pagamento (setVenda_id) antes de gravar");
        }

        String tipo = tipoDe(pagamento);

        exigirVendaSemPagamento(conn, pagamento.getVenda_id());

        String sql = "INSERT INTO pagamento " +
                "(venda_id, tipo, valor, data_pagamento) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagamento.getVenda_id());
            stmt.setString(2, tipo);
            stmt.setDouble(3, pagamento.getValor());

            LocalDate data = pagamento.getData_pagamento() != null ? pagamento.getData_pagamento() : LocalDate.now();
            stmt.setDate(4, java.sql.Date.valueOf(data));

            stmt.executeUpdate();

            // Recupera o ID gerado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setPagamento_id(rs.getInt(1));
                }
            }
        }

        inserirDadosDaSubclasse(conn, pagamento);
    }

    public Pagamento buscarPorVenda(Connection conn, int vendaID) throws SQLException {
        return carregar(conn, SQL_POR_VENDA, vendaID);
    }

    // Apaga primeiro as linhas das subclasses e depois a linha de pagamento
    // (o banco nao apaga em cascata)
    public void excluirPorVenda(Connection conn, int vendaID) throws SQLException {

        Integer pagamentoId = null;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT pagamento_id FROM pagamento WHERE venda_id = ?")) {
            stmt.setInt(1, vendaID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pagamentoId = rs.getInt(1);
                }
            }
        }

        if (pagamentoId == null) {
            return; // Nao ha pagamento registrado para esta venda
        }

        for (String tabela : TABELAS_SUBCLASSES) {
            String sql = "DELETE FROM " + tabela + " WHERE pagamento_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, pagamentoId);
                stmt.executeUpdate();
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM pagamento WHERE pagamento_id = ?")) {
            stmt.setInt(1, pagamentoId);
            stmt.executeUpdate();
        }
    }

    // =====================================================================
    // Gravacao: tabela da subclasse
    // =====================================================================

    private String tipoDe(Pagamento pagamento) {

        if (pagamento instanceof PagamentoDinheiro) {
            return "DINHEIRO";
        }
        if (pagamento instanceof PagamentoPix) {
            return "PIX";
        }
        if (pagamento instanceof PagamentoDebito) {
            return "DEBITO";
        }
        if (pagamento instanceof PagamentoCredito) {
            return "CREDITO";
        }
        throw new IllegalArgumentException("Tipo de pagamento desconhecido");
    }

    private void inserirDadosDaSubclasse(Connection conn, Pagamento pagamento)
            throws SQLException {

        int id = pagamento.getPagamento_id();

        if (pagamento instanceof PagamentoDinheiro din) {

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO pagamento_dinheiro (pagamento_id, valor_recebido) " +
                            "VALUES (?, ?)")) {
                stmt.setInt(1, id);
                stmt.setDouble(2, din.getValor_recebido());
                stmt.executeUpdate();
            }

        } else if (pagamento instanceof PagamentoPix pix) {

            if (pix.getChave_pix() == null || pix.getChave_pix().isBlank()) {
                throw new IllegalArgumentException("A chave Pix é obrigatória para registrar o pagamento");
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO pagamento_pix (pagamento_id, chave_pix, id_transacao) " +
                            "VALUES (?, ?, ?)")) {
                stmt.setInt(1, id);
                stmt.setString(2, pix.getChave_pix());
                stmt.setString(3, pix.getId_transacao()); // pode ser null
                stmt.executeUpdate();
            }

        } else if (pagamento instanceof PagamentoDebito deb) {

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO pagamento_debito (pagamento_id, bandeira, ultimos_digitos) " +
                            "VALUES (?, ?, ?)")) {
                stmt.setInt(1, id);
                stmt.setString(2, deb.getBandeira());
                stmt.setString(3, deb.getUltimos_digitos());
                stmt.executeUpdate();
            }

        } else if (pagamento instanceof PagamentoCredito cred) {

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO pagamento_credito " +
                            "(pagamento_id, bandeira, ultimos_digitos, numero_parcelas, taxa_juros) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, id);
                stmt.setString(2, cred.getBandeira());
                stmt.setString(3, cred.getUltimos_digitos());
                stmt.setInt(4, cred.getNumero_parcelas());
                stmt.setDouble(5, cred.getTaxa_juros());
                stmt.executeUpdate();
            }
        }
    }

    private void exigirVendaSemPagamento(Connection conn, int vendaID) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM pagamento WHERE venda_id = ?")) {

            stmt.setInt(1, vendaID);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new IllegalStateException(
                            "A venda " + vendaID + " já possui pagamento");
                }
            }
        }
    }

    // =====================================================================
    // Leitura
    // =====================================================================

    // Le a linha de "pagamento" e, pelo tipo, monta a subclasse certa.
    // Retorna null se nao encontrar.
    private Pagamento carregar(Connection conn, String sql, int parametro)
            throws SQLException {

        int pagamentoID;
        int vendaID;
        String tipo;
        double valor;
        java.sql.Date data;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                pagamentoID = rs.getInt("pagamento_id");
                vendaID = rs.getInt("venda_id");
                tipo = rs.getString("tipo");
                valor = rs.getDouble("valor");
                data = rs.getDate("data_pagamento");
            }
        }

        Pagamento pagamento;

        switch (tipo) {
            case "DINHEIRO" -> pagamento = carregarDinheiro(conn, pagamentoID, valor);
            case "PIX" -> pagamento = carregarPix(conn, pagamentoID, valor);
            case "DEBITO" -> pagamento = carregarDebito(conn, pagamentoID, valor);
            case "CREDITO" -> pagamento = carregarCredito(conn, pagamentoID, valor);
            default -> throw new IllegalStateException(
                    "Tipo de pagamento desconhecido no banco: " + tipo);
        }

        pagamento.setPagamento_id(pagamentoID);
        pagamento.setVenda_id(vendaID);

        if (data != null) {
            pagamento.setData_pagamento(data.toLocalDate());
        }

        return pagamento;
    }

    private Pagamento carregarDinheiro(Connection conn, int id, double valor)
            throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT valor_recebido FROM pagamento_dinheiro WHERE pagamento_id = ?")) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw dadosAusentes("dinheiro", id);
                }
                return new PagamentoDinheiro(valor, rs.getDouble("valor_recebido"));
            }
        }
    }

    private Pagamento carregarPix(Connection conn, int id, double valor)
            throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT chave_pix, id_transacao FROM pagamento_pix WHERE pagamento_id = ?")) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw dadosAusentes("Pix", id);
                }

                return new PagamentoPix(valor, rs.getString("chave_pix"), rs.getString("id_transacao"));
            }
        }
    }

    private Pagamento carregarDebito(Connection conn, int id, double valor)
            throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT bandeira, ultimos_digitos FROM pagamento_debito WHERE pagamento_id = ?")) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw dadosAusentes("débito", id);
                }
                return new PagamentoDebito(valor,
                        rs.getString("bandeira"), rs.getString("ultimos_digitos"));
            }
        }
    }

    private Pagamento carregarCredito(Connection conn, int id, double valor)
            throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT bandeira, ultimos_digitos, numero_parcelas " +
                        "FROM pagamento_credito WHERE pagamento_id = ?")) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw dadosAusentes("crédito", id);
                }
                return new PagamentoCredito(valor,
                        rs.getString("bandeira"),
                        rs.getString("ultimos_digitos"),
                        rs.getInt("numero_parcelas"));
            }
        }
    }

    private IllegalStateException dadosAusentes(String tipo, int id) {
        return new IllegalStateException(
                "Dados do pagamento em " + tipo + " não encontrados (pagamento " + id + ")");
    }

    // =====================================================================
    // Controle de transacao
    // =====================================================================

    private void restaurarId(Pagamento pagamento, int idOriginal) {
        if (pagamento != null) {
            pagamento.setPagamento_id(idOriginal);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignorada) {
        }
    }

    private void restaurarAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignorada) {
        }
    }
}
