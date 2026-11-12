package db;

import java.sql.*;

public class DataBase {

    private static Connection conn = null;   // A conexão é armazenada em uma varivel estatica, pertencendo a classe e nao ao objeto

    public static Connection getConnection() { // Cria a conexao caso nao exista, e se nao tiver como criar (banco desativado, login incorreto), gera a excecao RuntimeException
        try {
            if(conn==null) {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetojava", "root", "1234");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    public static void closeConnection(){ // Verifica se a conexao existe e, caso exista, fecha com o close(). Caso nao consiga fecahr, emite a excecao RuntimeException
        try {
            if(conn!=null)
                conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeResultSet(ResultSet rs){ // Verifica se existe um resultset. Caso exista, fecha com o close(). Caso nao consiga fechar, emite um RuntimeException
        try {
            if(rs!=null)
                rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeStatement(Statement st){ // Verifica se existe um resultset. Caso exista, fecha com o close(). Caso nao consiga fechar, emite um RuntimeException
        try {
            if(st!=null)
                st.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
