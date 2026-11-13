package model.DAO.impl;

import db.DataBase;
import model.DAO.ClienteDAO;
import model.Entities.Cliente;

import java.sql.*;

import model.Entities.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOJDBC implements ClienteDAO {

    // Cadastrar cliente
    @Override
    public void cadastrarCliente(Cliente cliente) {

        String sql = "INSERT INTO cliente " +
                "(nomeCliente, CPF, email, dataNasc) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNomeCliente());
            stmt.setString(2, cliente.getCPF());
            stmt.setString(3, cliente.getEmail());
            stmt.setDate(4,
                    java.sql.Date.valueOf(cliente.getDataNasc()));

            stmt.executeUpdate();

            // Recupera o ID gerado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setClientID(rs.getInt(1));
                }
            }

            System.out.println("Cliente cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar cliente: " + e.getMessage(), e);
        }
    }

    // Atualizar cliente
    @Override
    public void atualizarCliente(Cliente cliente) {

        String sql = "UPDATE cliente SET " +
                "nomeCliente = ?, CPF = ?, email = ?, dataNasc = ? " +
                "WHERE clientID = ?";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNomeCliente());
            stmt.setString(2, cliente.getCPF());
            stmt.setString(3, cliente.getEmail());
            stmt.setDate(4,
                    java.sql.Date.valueOf(cliente.getDataNasc()));
            stmt.setInt(5, cliente.getClientID());

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Cliente atualizado com sucesso!");
            } else {
                System.out.println("Cliente não encontrado.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar cliente: " + e.getMessage(), e);
        }
    }

    // Buscar cliente pelo CPF
    @Override
    public Cliente buscarPorCpf(String cpf) {

        String sql = "SELECT * FROM cliente WHERE CPF = ?";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

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

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar cliente: " + e.getMessage(), e);
        }

        return null;
    }

    // Listar compras realizadas pelo cliente
    @Override
    public List<Venda> listarCompras(int clienteID) {
//
//        VendaDAOJDBC vendaDAO = new VendaDAOJDBC();
//
//        return vendaDAO.listarPorClienteID(clienteID);

        return List.of();
    }
}