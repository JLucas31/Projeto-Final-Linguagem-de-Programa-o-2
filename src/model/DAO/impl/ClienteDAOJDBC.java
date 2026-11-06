package model.DAO.impl;

import db.DataBase;
import model.DAO.ClienteDAO;
import model.Entities.Cliente;

import java.sql.*;

public abstract class ClienteDAOJDBC implements ClienteDAO {

    private final Connection conn;

    public ClienteDAOJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void cadastrarCliente(Cliente cliente) {

        PreparedStatement st = null; // Pra  coisas que vêm do código
        ResultSet rs = null; // Pra receber coisas do banco
        try {
            st = conn.prepareStatement("insert into Cliente(cliente, CPF, email, dataNasc values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            st.setString(1, cliente.getNomeCliente());
            st.setString(2, cliente.getCPF());
            st.setString(3, cliente.getEmail());
            st.setDate(4, Date.valueOf(cliente.getDataNasc()));
            int linhas = st.executeUpdate();

            if(linhas > 0){
                rs = st.getGeneratedKeys();
                while (rs.next()){
                    cliente.setClientID(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally{
            DataBase.closeResultSet(rs);
            DataBase.closeStatement(st);
        }

    }
}