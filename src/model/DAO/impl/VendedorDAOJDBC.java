package model.DAO.impl;

import db.DataBase;
import model.DAO.VendedorDAO;
import model.Entities.Vendedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VendedorDAOJDBC implements VendedorDAO {

    // Cadastrar vendedor
    @Override
    public void cadastrarVendedor(Vendedor vendedor) {

        String sql = "INSERT INTO vendedor " +
                "(nomeVendedor, telefone, dataNasc, " +
                "salarioBase, percentualComissao, totalComissao) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vendedor.getNomeVendedor());
            stmt.setString(2, vendedor.getTelefone());

            if (vendedor.getDataNasc() != null) {
                stmt.setDate(3,
                        java.sql.Date.valueOf(vendedor.getDataNasc()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setDouble(4, vendedor.getSalarioBase());
            stmt.setDouble(5, vendedor.getPercentualComissao());
            stmt.setDouble(6, vendedor.getTotalComissao());

            stmt.executeUpdate();

            // Recupera o ID gerado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    vendedor.setVendedorID(rs.getInt(1));
                }
            }

            System.out.println("Vendedor cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar vendedor: " + e.getMessage(), e);
        }
    }

    // Buscar vendedor pelo ID
    @Override
    public Vendedor buscarPorID(int id) {

        String sql = "SELECT * FROM vendedor WHERE vendedorID = ?";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Vendedor vendedor = criarVendedor(rs);

                    // Recupera os IDs das vendas do vendedor
                    carregarVendas(conn, vendedor);

                    return vendedor;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar vendedor: " + e.getMessage(), e);
        }

        return null;
    }

    // Listar todos os vendedores
    @Override
    public List<Vendedor> listarVendedores() {

        List<Vendedor> vendedores = new ArrayList<>();

        String sql = "SELECT * FROM vendedor";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Vendedor vendedor = criarVendedor(rs);

                carregarVendas(conn, vendedor);

                vendedores.add(vendedor);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar vendedores: " + e.getMessage(), e);
        }

        return vendedores;
    }

    // Atualizar comissão conforme as vendas finalizadas
    @Override
    public double atualizarComissao(int vendedorID) {

        String sqlTotal = "SELECT COALESCE(SUM(valor_total), 0) " +
                "FROM venda " +
                "WHERE vendedorID = ? AND finalizada = TRUE";

        String sqlAtualizar = "UPDATE vendedor " +
                "SET totalComissao = ? " +
                "WHERE vendedorID = ?";

        try (Connection conn = DataBase.getConnection()) {

            double totalVendido;

            // Consulta o total vendido
            try (PreparedStatement stmt =
                         conn.prepareStatement(sqlTotal)) {

                stmt.setInt(1, vendedorID);

                try (ResultSet rs = stmt.executeQuery()) {

                    rs.next();
                    totalVendido = rs.getDouble(1);
                }
            }

            // Busca o percentual de comissão
            String sqlPercentual =
                    "SELECT percentualComissao FROM vendedor " +
                            "WHERE vendedorID = ?";

            double percentual;

            try (PreparedStatement stmt =
                         conn.prepareStatement(sqlPercentual)) {

                stmt.setInt(1, vendedorID);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "Vendedor não encontrado");
                    }

                    percentual = rs.getDouble("percentualComissao");
                }
            }

            // Calcula a comissão
            double comissao =
                    totalVendido * (percentual / 100);

            // Atualiza a comissão no banco
            try (PreparedStatement stmt =
                         conn.prepareStatement(sqlAtualizar)) {

                stmt.setDouble(1, comissao);
                stmt.setInt(2, vendedorID);

                stmt.executeUpdate();
            }

            return comissao;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar comissão: " + e.getMessage(), e);
        }
    }

    // Converte o resultado SQL em objeto Vendedor
    private Vendedor criarVendedor(ResultSet rs)
            throws SQLException {

        Vendedor vendedor = new Vendedor();

        vendedor.setVendedorID(rs.getInt("vendedorID"));
        vendedor.setNomeVendedor(rs.getString("nomeVendedor"));
        vendedor.setTelefone(rs.getString("telefone"));

        java.sql.Date data = rs.getDate("dataNasc");

        if (data != null) {
            vendedor.setDataNasc(data.toLocalDate());
        }

        vendedor.setSalarioBase(rs.getDouble("salarioBase"));

        vendedor.setPercentualComissao(
                rs.getDouble("percentualComissao"));

        vendedor.setTotalComissao(
                rs.getDouble("totalComissao"));

        return vendedor;
    }

    // Recupera os IDs das vendas relacionadas ao vendedor
    private void carregarVendas(Connection conn, Vendedor vendedor)
            throws SQLException {

        String sql = "SELECT venda_id FROM venda WHERE vendedorID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendedor.getVendedorID());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    vendedor.adicionarVenda(rs.getInt("venda_id"));
                }
            }
        }
    }
}
