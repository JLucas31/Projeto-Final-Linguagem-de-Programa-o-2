package model.DAO.impl;

import db.DataBase;
import model.DAO.VendaDAO;
import model.Entities.Venda;

import java.sql.*;

public abstract class VendaDAOJDBC implements VendaDAO {

    private final Connection conn;

    public VendaDAOJDBC(Connection conn) {
        this.conn = conn;
    }
    PreparedStatement st = null; // Sao as intrucoes do SQL feitas no java que vao para o MySQL
    ResultSet rs = null; // Eh o retorno do select do BD

    @Override
    public void inserirVenda(Venda venda) {

        PreparedStatement st = null; // Pra  coisas que vêm do código
        ResultSet rs = null; // Pra receber coisas do banco
        try {
            st = conn.prepareStatement("insert into Venda(produto, cliente, vendedor, data_venda, valor_total values (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            st.setString(1, venda.getCliente().getNomeCliente());
            st.setString(2, venda.getCliente().getCPF());
            st.setDate(3, Date.valueOf(venda.getData_venda()));
            int linhas = st.executeUpdate();

            if(linhas > 0){
                rs = st.getGeneratedKeys();
                while (rs.next()){
                    venda.setVenda_id(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally{
            DataBase.closeResultSet(rs);
            DataBase.closeStatement(st);
        }

    }

    public void atualizarVenda(Venda venda){

        PreparedStatement st = null;
        ResultSet rs = null;


    }
}
