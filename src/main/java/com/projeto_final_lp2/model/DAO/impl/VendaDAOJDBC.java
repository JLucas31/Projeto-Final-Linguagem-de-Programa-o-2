package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.VendaDAO;
import com.projeto_final_lp2.model.Entities.Cliente;
import com.projeto_final_lp2.model.Entities.ItemVenda;
import com.projeto_final_lp2.model.Entities.Pagamento;
import com.projeto_final_lp2.model.Entities.PagamentoCredito;
import com.projeto_final_lp2.model.Entities.PagamentoDebito;
import com.projeto_final_lp2.model.Entities.PagamentoDinheiro;
import com.projeto_final_lp2.model.Entities.PagamentoPix;
import com.projeto_final_lp2.model.Entities.Produto;
import com.projeto_final_lp2.model.Entities.Venda;
import com.projeto_final_lp2.model.Entities.Vendedor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Venda. Cada venda envolve 3 tabelas (venda, item_venda, pagamento),
 * entao as operacoes de escrita rodam dentro de uma transacao: ou grava tudo,
 * ou nao grava nada.
 *
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 */
public class VendaDAOJDBC implements VendaDAO {


    private static final String SQL_IDS_TODAS =
            "SELECT venda_id FROM venda ORDER BY data_venda DESC, venda_id DESC";
    private static final String SQL_IDS_POR_CLIENTE =
            "SELECT venda_id FROM venda WHERE clientID = ? ORDER BY data_venda DESC, venda_id DESC";

    private Connection conn = null;

    // =====================================================================
    // Operacoes publicas (VendaDAO)
    // =====================================================================

    // Grava a venda, seus itens e o pagamento (se houver) de uma vez.
    // Se a venda ja chegar finalizada, tambem baixa o estoque no banco.
    @Override
    public void inserirVenda(Venda venda) {

        validarVenda(venda);

        int idOriginal = venda.getVenda_id();
        conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO venda " +
                    "(clientID, vendedorID, data_venda, valor_total, finalizada) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, venda.getCliente().getClientID());
                stmt.setInt(2, venda.getVendedor().getVendedorID());
                stmt.setDate(3, java.sql.Date.valueOf(venda.getData_venda()));
                stmt.setDouble(4, venda.getValor_total());
                stmt.setBoolean(5, venda.isFinalizada());

                stmt.executeUpdate();

                // Recupera o ID gerado pelo MySQL
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        venda.setVenda_id(rs.getInt(1));
                    }
                }
            }

            sincronizarItens(conn, venda);

            if (venda.getPagamento() != null) {
                inserirPagamento(conn, venda);
            }

            if (venda.isFinalizada()) {
                baixarEstoque(conn, venda);
            }

            conn.commit();
            System.out.println("Venda cadastrada com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            desfazerIdsEmMemoria(venda, idOriginal);
            throw new RuntimeException(
                    "Erro ao cadastrar venda: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            desfazerIdsEmMemoria(venda, idOriginal);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // Sincroniza os itens da venda em memoria com o banco:
    // novos itens (id 0) sao inseridos, itens existentes tem qtd/valor
    // atualizados e itens removidos da venda saem do banco.
    @Override
    public void adicionarItem(Venda venda) {

        exigirVendaPersistida(venda);

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            exigirVendaAberta(conn, venda.getVenda_id());
            sincronizarItens(conn, venda);

            conn.commit();
            System.out.println("Itens da venda atualizados com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(
                    "Erro ao atualizar itens da venda: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // Troca a forma de pagamento: apaga o pagamento antigo e grava o atual
    @Override
    public void alterarPagamento(Venda venda) {

        exigirVendaPersistida(venda);

        if (venda.getPagamento() == null) {
            throw new IllegalArgumentException(
                    "A venda nao possui pagamento para gravar");
        }

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            exigirVendaAberta(conn, venda.getVenda_id());

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM pagamento WHERE venda_id = ?")) {
                stmt.setInt(1, venda.getVenda_id());
                stmt.executeUpdate();
            }

            inserirPagamento(conn, venda);

            conn.commit();
            System.out.println("Pagamento alterado com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(
                    "Erro ao alterar pagamento: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // Exclui a venda com itens e pagamento. Venda finalizada nao pode ser
    // excluida (ja baixou estoque e gerou comissao).
    @Override
    public void excluirVenda(Venda venda) {

        exigirVendaPersistida(venda);

        Connection conn = DataBase.getConnection();

        try {
            conn.setAutoCommit(false);

            exigirVendaAberta(conn, venda.getVenda_id());

            executarDeletePorVenda(conn, "DELETE FROM pagamento WHERE venda_id = ?",
                    venda.getVenda_id());
            executarDeletePorVenda(conn, "DELETE FROM item_venda WHERE venda_id = ?",
                    venda.getVenda_id());
            executarDeletePorVenda(conn, "DELETE FROM venda WHERE venda_id = ?",
                    venda.getVenda_id());

            conn.commit();
            System.out.println("Venda excluida com sucesso!");

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(
                    "Erro ao excluir venda: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    // Busca a venda completa (cliente, vendedor, itens e pagamento)
    // pelo ID da venda informada. Retorna null se nao existir.
    @Override
    public Venda buscarVenda(Venda venda) {

        if (venda == null) {
            throw new IllegalArgumentException("Venda nao pode ser nula");
        }

        Connection conn = DataBase.getConnection();

        try {
            return carregarVenda(conn, venda.getVenda_id());

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar venda: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Venda> listarVendas() {
        return listar(SQL_IDS_TODAS, null, "Erro ao listar vendas: ");
    }

    // Usado por ClienteDAOJDBC.listarCompras
    @Override
    public List<Venda> listarPorClienteID(int clienteID) {
        return listar(SQL_IDS_POR_CLIENTE, clienteID,
                "Erro ao listar compras do cliente: ");
    }

    // =====================================================================
    // Escrita (metodos auxiliares, sempre dentro de uma transacao aberta)
    // =====================================================================

    private void sincronizarItens(Connection conn, Venda venda) throws SQLException {

        int vendaId = venda.getVenda_id();

        // 1) Remove do banco os itens que nao estao mais na venda
        List<Integer> idsMantidos = new ArrayList<>();
        for (ItemVenda item : venda.getItens()) {
            if (item.getItem_venda_id() > 0) {
                idsMantidos.add(item.getItem_venda_id());
            }
        }

        StringBuilder sqlRemover =
                new StringBuilder("DELETE FROM item_venda WHERE venda_id = ?");

        if (!idsMantidos.isEmpty()) {
            sqlRemover.append(" AND item_venda_id NOT IN (");
            sqlRemover.append("?,".repeat(idsMantidos.size()));
            sqlRemover.setLength(sqlRemover.length() - 1); // tira a ultima virgula
            sqlRemover.append(")");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sqlRemover.toString())) {
            int i = 1;
            stmt.setInt(i++, vendaId);
            for (int id : idsMantidos) {
                stmt.setInt(i++, id);
            }
            stmt.executeUpdate();
        }

        // 2) Insere os itens novos e atualiza os que ja existiam
        String sqlInserir = "INSERT INTO item_venda " +
                "(venda_id, produto_id, qtd, valor_unitario) VALUES (?, ?, ?, ?)";

        String sqlAtualizar = "UPDATE item_venda " +
                "SET qtd = ?, valor_unitario = ? " +
                "WHERE item_venda_id = ? AND venda_id = ?";

        try (PreparedStatement inserir =
                     conn.prepareStatement(sqlInserir, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement atualizar = conn.prepareStatement(sqlAtualizar)) {

            for (ItemVenda item : venda.getItens()) {

                if (item.getItem_venda_id() == 0) {

                    inserir.setInt(1, vendaId);
                    inserir.setInt(2, item.getProduto().getProduto_id());
                    inserir.setInt(3, item.getQtd());
                    inserir.setDouble(4, item.getValor_unitario());
                    inserir.executeUpdate();

                    try (ResultSet rs = inserir.getGeneratedKeys()) {
                        if (rs.next()) {
                            item.setItem_venda_id(rs.getInt(1));
                        }
                    }

                } else {

                    atualizar.setInt(1, item.getQtd());
                    atualizar.setDouble(2, item.getValor_unitario());
                    atualizar.setInt(3, item.getItem_venda_id());
                    atualizar.setInt(4, vendaId);
                    atualizar.executeUpdate();
                }
            }
        }

        // 3) Mantem o total da venda no banco igual ao calculado em memoria
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE venda SET valor_total = ? WHERE venda_id = ?")) {
            stmt.setDouble(1, venda.getValor_total());
            stmt.setInt(2, vendaId);
            stmt.executeUpdate();
        }
    }

    // Todos os tipos de pagamento ficam na mesma tabela; a coluna "tipo"
    // diz qual subclasse de Pagamento e, e as colunas que nao se aplicam
    // ao tipo ficam NULL.
    private void inserirPagamento(Connection conn, Venda venda) throws SQLException {

        Pagamento pagamento = venda.getPagamento();

        String tipo;
        Double valorRecebido = null;
        String bandeira = null;
        String ultimosDigitos = null;
        Integer parcelas = null;
        Double taxaJuros = null;

        if (pagamento instanceof PagamentoDinheiro din) {
            tipo = "DINHEIRO";
            valorRecebido = din.getValor_recebido();
        } else if (pagamento instanceof PagamentoPix pix) {
            tipo = "PIX";
            valorRecebido = pix.getValor_recebido();
        } else if (pagamento instanceof PagamentoDebito deb) {
            tipo = "DEBITO";
            bandeira = deb.getBandeira();
            ultimosDigitos = deb.getUltimos_digitos();
        } else if (pagamento instanceof PagamentoCredito cred) {
            tipo = "CREDITO";
            bandeira = cred.getBandeira();
            ultimosDigitos = cred.getUltimos_digitos();
            parcelas = cred.getNumero_parcelas();
            taxaJuros = cred.getTaxa_juros();
        } else {
            throw new IllegalArgumentException("Tipo de pagamento desconhecido");
        }

        String sql = "INSERT INTO pagamento " +
                "(venda_id, tipo, valor, valor_recebido, bandeira, " +
                "ultimos_digitos, numero_parcelas, taxa_juros, data_pagamento) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, venda.getVenda_id());
            stmt.setString(2, tipo);
            stmt.setDouble(3, pagamento.getValor());
            stmt.setObject(4, valorRecebido);
            stmt.setString(5, bandeira);
            stmt.setString(6, ultimosDigitos);
            stmt.setObject(7, parcelas);
            stmt.setObject(8, taxaJuros);
            stmt.setDate(9, java.sql.Date.valueOf(pagamento.getData_pagamento()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setPagamento_id(rs.getInt(1));
                }
            }

            pagamento.setVenda_id(venda.getVenda_id());
        }
    }

    // Baixa no banco a quantidade vendida de cada item. A condicao
    // "quantidade >= ?" impede estoque negativo se outro caixa vendeu antes.
    private void baixarEstoque(Connection conn, Venda venda) throws SQLException {

        String sql = "UPDATE produto SET quantidade = quantidade - ? " +
                "WHERE produto_id = ? AND quantidade >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (ItemVenda item : venda.getItens()) {

                stmt.setInt(1, item.getQtd());
                stmt.setInt(2, item.getProduto().getProduto_id());
                stmt.setInt(3, item.getQtd());

                if (stmt.executeUpdate() == 0) {
                    throw new IllegalStateException(
                            "Estoque insuficiente no banco para o produto "
                                    + item.getProduto().getProduto_id());
                }
            }
        }
    }

    private void executarDeletePorVenda(Connection conn, String sql, int vendaId)
            throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendaId);
            stmt.executeUpdate();
        }
    }

    // =====================================================================
    // Leitura (metodos auxiliares)
    // =====================================================================

    private List<Venda> listar(String sqlIds, Integer parametro, String msgErro) {

        Connection conn = DataBase.getConnection();

        try {
            // Primeiro le so os IDs, depois monta cada venda completa
            List<Integer> ids = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sqlIds)) {

                if (parametro != null) {
                    stmt.setInt(1, parametro);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                }
            }

            List<Venda> vendas = new ArrayList<>();

            for (int id : ids) {
                Venda venda = carregarVenda(conn, id);
                if (venda != null) {
                    vendas.add(venda);
                }
            }

            return vendas;

        } catch (SQLException e) {
            throw new RuntimeException(msgErro + e.getMessage(), e);
        }
    }

    // Monta a venda completa a partir do ID. Retorna null se nao existir.
    private Venda carregarVenda(Connection conn, int vendaId) throws SQLException {

        int clientID;
        int vendedorID;
        LocalDate dataVenda;
        boolean finalizada;

        String sql = "SELECT clientID, vendedorID, data_venda, finalizada " +
                "FROM venda WHERE venda_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendaId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                clientID = rs.getInt("clientID");
                vendedorID = rs.getInt("vendedorID");
                dataVenda = rs.getDate("data_venda").toLocalDate();
                finalizada = rs.getBoolean("finalizada");
            }
        }

        Cliente cliente = carregarCliente(conn, clientID);
        Vendedor vendedor = carregarVendedor(conn, vendedorID);

        Venda venda = new Venda(vendaId, cliente, vendedor, dataVenda);

        carregarItens(conn, venda);
        venda.carregarPagamento(carregarPagamento(conn, vendaId));
        venda.carregarFinalizada(finalizada);

        return venda;
    }

    private Cliente carregarCliente(Connection conn, int clientID) throws SQLException {

        String sql = "SELECT * FROM cliente WHERE clientID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientID);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Cliente cliente = new Cliente();

                cliente.setClientID(rs.getInt("clientID"));
                cliente.setNomeCliente(rs.getString("nomeCliente"));
                cliente.setCPF(rs.getString("CPF"));
                cliente.setEmail(rs.getString("email"));

                java.sql.Date data = rs.getDate("dataNasc");
                if (data != null) {
                    cliente.setDataNasc(data.toLocalDate());
                }

                return cliente;
            }
        }
    }

    // Carrega so os dados do vendedor (sem a lista de vendas, para nao
    // criar um ciclo Venda -> Vendedor -> Venda)
    private Vendedor carregarVendedor(Connection conn, int vendedorID) throws SQLException {

        String sql = "SELECT * FROM vendedor WHERE vendedorID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendedorID);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Vendedor vendedor = new Vendedor();

                vendedor.setVendedorID(rs.getInt("vendedorID"));
                vendedor.setNomeVendedor(rs.getString("nomeVendedor"));
                vendedor.setTelefone(rs.getString("telefone"));

                java.sql.Date data = rs.getDate("dataNasc");
                if (data != null) {
                    vendedor.setDataNasc(data.toLocalDate());
                }

                vendedor.setSalarioBase(rs.getDouble("salarioBase"));
                vendedor.setPercentualComissao(rs.getDouble("percentualComissao"));
                vendedor.setTotalComissao(rs.getDouble("totalComissao"));

                return vendedor;
            }
        }
    }

    private void carregarItens(Connection conn, Venda venda) throws SQLException {

        String sql = "SELECT i.item_venda_id, i.qtd, i.valor_unitario, " +
                "p.produto_id, p.nomeProduto, p.marca, p.valor, p.quantidade, p.lote " +
                "FROM item_venda i " +
                "JOIN produto p ON p.produto_id = i.produto_id " +
                "WHERE i.venda_id = ? " +
                "ORDER BY i.item_venda_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venda.getVenda_id());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Produto produto = new Produto(
                            rs.getInt("produto_id"),
                            rs.getString("nomeProduto"),
                            rs.getString("marca"),
                            rs.getDouble("valor"),
                            rs.getInt("quantidade"),
                            rs.getString("lote"));

                    // valor_unitario eh o preco NA HORA da venda,
                    // nao o preco atual do produto
                    ItemVenda item = new ItemVenda(
                            produto,
                            rs.getInt("qtd"),
                            rs.getDouble("valor_unitario"));

                    item.setItem_venda_id(rs.getInt("item_venda_id"));

                    venda.carregarItem(item);
                }
            }
        }
    }

    // Reconstroi a subclasse correta de Pagamento a partir da coluna "tipo".
    // Retorna null se a venda ainda nao tem pagamento.
    private Pagamento carregarPagamento(Connection conn, int vendaId) throws SQLException {

        String sql = "SELECT * FROM pagamento WHERE venda_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendaId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String tipo = rs.getString("tipo");
                double valor = rs.getDouble("valor");

                Pagamento pagamento;

                switch (tipo) {
                    case "DINHEIRO" -> pagamento =
                            new PagamentoDinheiro(valor, rs.getDouble("valor_recebido"));
                    case "PIX" -> pagamento =
                            new PagamentoPix(valor, rs.getDouble("valor_recebido"));
                    case "DEBITO" -> pagamento = new PagamentoDebito(
                            valor,
                                    rs.getString("bandeira"),
                                    rs.getString("ultimos_digitos"));
                    case "CREDITO" -> pagamento = new PagamentoCredito(
                            valor,
                            rs.getString("bandeira"),
                            rs.getString("ultimos_digitos"),
                            rs.getInt("numero_parcelas"));
                    default -> throw new IllegalStateException(
                            "Tipo de pagamento desconhecido no banco: " + tipo);
                }

                pagamento.setPagamento_id(rs.getInt("pagamento_id"));
                pagamento.setVenda_id(vendaId);

                java.sql.Date data = rs.getDate("data_pagamento");
                if (data != null) {
                    pagamento.setData_pagamento(data.toLocalDate());
                }

                return pagamento;
            }
        }
    }

    // =====================================================================
    // Validacoes e controle de transacao
    // =====================================================================

    private void validarVenda(Venda venda) {

        if (venda == null) {
            throw new IllegalArgumentException("Venda nao pode ser nula");
        }
        if (venda.getCliente() == null || venda.getCliente().getClientID() <= 0) {
            throw new IllegalArgumentException(
                    "A venda precisa de um cliente ja cadastrado");
        }
        if (venda.getVendedor() == null || venda.getVendedor().getVendedorID() <= 0) {
            throw new IllegalArgumentException(
                    "A venda precisa de um vendedor ja cadastrado");
        }
        if (venda.getData_venda() == null) {
            throw new IllegalArgumentException("A venda precisa de uma data");
        }
    }

    private void exigirVendaPersistida(Venda venda) {

        if (venda == null) {
            throw new IllegalArgumentException("Venda nao pode ser nula");
        }
        if (venda.getVenda_id() <= 0) {
            throw new IllegalArgumentException(
                    "A venda ainda nao foi gravada no banco (use inserirVenda)");
        }
    }

    // Confere NO BANCO se a venda existe e ainda esta aberta
    private void exigirVendaAberta(Connection conn, int vendaId) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT finalizada FROM venda WHERE venda_id = ?")) {

            stmt.setInt(1, vendaId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException("Venda nao encontrada");
                }
                if (rs.getBoolean("finalizada")) {
                    throw new IllegalStateException(
                            "Venda ja finalizada: nao pode ser alterada");
                }
            }
        }
    }

    // Se a transacao falhou, os IDs gerados em memoria nao existem no banco
    private void desfazerIdsEmMemoria(Venda venda, int idOriginal) {

        venda.setVenda_id(idOriginal);

        for (ItemVenda item : venda.getItens()) {
            item.setItem_venda_id(0);
        }
        if (venda.getPagamento() != null) {
            venda.getPagamento().setPagamento_id(0);
            venda.getPagamento().setVenda_id(0);
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