package com.projeto_final_lp2.model.DAO.impl;

import com.projeto_final_lp2.db.DataBase;
import com.projeto_final_lp2.model.DAO.ProdutoDAO;
import com.projeto_final_lp2.model.Entities.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Produto (tabela produto).
 *
 * A conexao pertence a classe DataBase, por isso este DAO NAO a fecha.
 *
 * O metodo criarProduto(ResultSet) eh reaproveitado pelos DAOs que fazem
 * JOIN com a tabela produto (NotaFiscalCompraDAOJDBC, ItemVendaDAOJDBC,
 * ItemNotaDAOJDBC): o SELECT deles precisa trazer as colunas
 * produto_id, nomeProduto, marca, valor, quantidade, ultima_data_compra,
 * setor e quantidade_minima.
 */
public class ProdutoDAOJDBC implements ProdutoDAO {

    // Colunas lidas por criarProduto (usadas nos SELECTs deste DAO)
    private static final String COLUNAS =
            "produto_id, nomeProduto, marca, valor, quantidade, ultima_data_compra, setor, quantidade_minima";

    // Cadastrar produto
    @Override
    public void cadastrarProduto(Produto produto) {

        validarProduto(produto);

        String sql = "INSERT INTO produto " +
                "(nomeProduto, marca, valor, quantidade, ultima_data_compra, setor, quantidade_minima) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNomeProduto());
            stmt.setString(2, produto.getMarca());
            stmt.setDouble(3, produto.getValor());
            stmt.setInt(4, produto.getQuantidade());

            if (produto.getUltimaDataCompra() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(produto.getUltimaDataCompra()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setString(6, produto.getSetor());
            stmt.setInt(7, produto.getQuantidadeMinima());

            stmt.executeUpdate();

            // Recupera o ID gerado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setProduto_id(rs.getInt(1));
                }
            }

            System.out.println("Produto cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar produto: " + e.getMessage(), e);
        }
    }

    // Atualizar produto (nao altera ultima_data_compra). A quantidade minima
    // tambem eh gravada aqui.
    @Override
    public void atualizarProduto(Produto produto) {

        validarProduto(produto);

        String sql = "UPDATE produto SET " +
                "nomeProduto = ?, marca = ?, valor = ?, quantidade = ?, setor = ?, " +
                "quantidade_minima = ? " +
                "WHERE produto_id = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getNomeProduto());
            stmt.setString(2, produto.getMarca());
            stmt.setDouble(3, produto.getValor());
            stmt.setInt(4, produto.getQuantidade());
            stmt.setString(5, produto.getSetor());
            stmt.setInt(6, produto.getQuantidadeMinima());
            stmt.setInt(7, produto.getProduto_id());

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Produto atualizado com sucesso!");
            } else {
                System.out.println("Produto não encontrado.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar produto: " + e.getMessage(), e);
        }
    }

    // Remover produto
    @Override
    public void removerProduto(int id) {

        String sql = "DELETE FROM produto WHERE produto_id = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Produto removido com sucesso!");
            } else {
                System.out.println("Produto não encontrado.");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // item_venda / item_nota ainda apontam para este produto
            throw new IllegalStateException(
                    "Não é possível remover o produto " + id +
                            ": ele já aparece em vendas, notas fiscais ou cotações", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao remover produto: " + e.getMessage(), e);
        }
    }

    // Retorna null se nao existir
    @Override
    public Produto buscarPorID(int id) {

        String sql = "SELECT " + COLUNAS + " FROM produto WHERE produto_id = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return criarProduto(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar produto: " + e.getMessage(), e);
        }

        return null;
    }

    // Busca por parte do nome
    @Override
    public List<Produto> buscarPorNome(String nome) {

        exigirTexto(nome, "nome");

        return listar("SELECT " + COLUNAS + " FROM produto " +
                        "WHERE LOWER(nomeProduto) LIKE ? ORDER BY nomeProduto",
                "%" + nome.trim().toLowerCase() + "%");
    }

    // Busca por parte do nome da marca
    @Override
    public List<Produto> buscarPorMarca(String marca) {

        exigirTexto(marca, "marca");

        return listar("SELECT " + COLUNAS + " FROM produto " +
                        "WHERE LOWER(marca) LIKE ? ORDER BY marca, nomeProduto",
                "%" + marca.trim().toLowerCase() + "%");
    }

    // Busca pelo setor exato
    @Override
    public List<Produto> buscarPorSetor(String setor) {

        exigirTexto(setor, "setor");

        return listar("SELECT " + COLUNAS + " FROM produto " +
                        "WHERE LOWER(setor) = ? ORDER BY nomeProduto",
                setor.trim().toLowerCase());
    }

    // Listar todos os produtos
    @Override
    public List<Produto> listarProdutos() {

        return listar("SELECT " + COLUNAS + " FROM produto ORDER BY nomeProduto");
    }

    // Produtos com estoque menor ou igual ao limite, os mais baixos primeiro
    @Override
    public List<Produto> listarEstoqueBaixo(int limite) {

        if (limite < 0) {
            throw new IllegalArgumentException("O limite não pode ser negativo");
        }

        String sql = "SELECT " + COLUNAS + " FROM produto " +
                "WHERE quantidade <= ? ORDER BY quantidade, nomeProduto";

        List<Produto> produtos = new ArrayList<>();

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(criarProduto(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar produtos com estoque baixo: " + e.getMessage(), e);
        }

        return produtos;
    }

    // Baixa o estoque direto no banco. A condicao "quantidade >= ?" faz a
    // checagem e a baixa no mesmo UPDATE, entao duas baixas ao mesmo tempo
    // nao deixam o estoque negativo.
    @Override
    public void baixarEstoque(int id, int qtd) {

        exigirQuantidadePositiva(qtd);

        String sql = "UPDATE produto SET quantidade = quantidade - ? " +
                "WHERE produto_id = ? AND quantidade >= ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, qtd);
            stmt.setInt(2, id);
            stmt.setInt(3, qtd);

            if (stmt.executeUpdate() == 0) {
                // Nenhuma linha mudou: o produto nao existe ou nao tem estoque
                if (buscarPorID(id) == null) {
                    throw new IllegalArgumentException("Produto não encontrado: " + id);
                }
                throw new IllegalStateException("Estoque insuficiente para o produto " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao baixar estoque: " + e.getMessage(), e);
        }
    }

    // Ajuste manual de estoque (a entrada de nota fiscal tem seu proprio fluxo)
    @Override
    public void adicionarEstoque(int id, int qtd) {

        exigirQuantidadePositiva(qtd);

        String sql = "UPDATE produto SET quantidade = quantidade + ? WHERE produto_id = ?";

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, qtd);
            stmt.setInt(2, id);

            if (stmt.executeUpdate() == 0) {
                throw new IllegalArgumentException("Produto não encontrado: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao adicionar estoque: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    // Converte a linha atual do ResultSet em Produto. Tambem eh usado pelos
    // DAOs que fazem JOIN com produto (ver comentario da classe).
    static Produto criarProduto(ResultSet rs) throws SQLException {

        java.sql.Date data = rs.getDate("ultima_data_compra");

        return new Produto(
                rs.getInt("produto_id"),
                rs.getString("nomeProduto"),
                rs.getString("marca"),
                rs.getDouble("valor"),
                rs.getInt("quantidade"),
                rs.getString("setor"),
                data != null ? data.toLocalDate() : null,
                rs.getInt("quantidade_minima"));
    }

    // Executa um SELECT de produtos cujos parametros (se houver) sao todos texto
    private List<Produto> listar(String sql, String... parametros) {

        List<Produto> produtos = new ArrayList<>();

        Connection conn = DataBase.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                stmt.setString(i + 1, parametros[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(criarProduto(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar produtos: " + e.getMessage(), e);
        }

        return produtos;
    }

    private void validarProduto(Produto produto) {

        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        exigirTexto(produto.getNomeProduto(), "nome do produto");
        exigirTexto(produto.getSetor(), "setor");

        if (produto.getValor() < 0) {
            throw new IllegalArgumentException("O valor do produto não pode ser negativo");
        }
        if (produto.getQuantidade() < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa");
        }
        if (produto.getQuantidadeMinima() < 0) {
            throw new IllegalArgumentException("A quantidade mínima não pode ser negativa");
        }
    }

    private void exigirTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Informe o " + campo);
        }
    }

    private void exigirQuantidadePositiva(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}
