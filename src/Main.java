import model.DAO.VendedorDAO;
import model.DAO.impl.VendedorDAOJDBC;
import model.Entities.Cliente;
import model.Entities.Vendedor;
import model.DAO.impl.ClienteDAOJDBC;
import model.DAO.ClienteDAO;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        ClienteDAO clienteDAO = new ClienteDAOJDBC();

        // Criando cliente
        Cliente cliente = new Cliente();

//        cliente.setNomeCliente("Adalto");
//        cliente.setCPF("123433333");
//        cliente.setEmail("Lula@gmail.com");
//        cliente.setDataNasc(LocalDate.of(2000, 5, 10));
//
//        // Cadastrando no banco
//        clienteDAO.cadastrarCliente(cliente);

        // Buscando pelo CPF
        Cliente encontrado =
                clienteDAO.buscarPorCpf("123433333");

        if (encontrado != null) {
            System.out.println(encontrado.getNomeCliente());
        }
    }
}

//Aqui em baixo é o código para testar o vendedor, so cortar e colar na main

////        // Criando vendedor
////        Vendedor vendedor = new Vendedor(
////                "Adryan",
////                "8555555",
////                LocalDate.of(1800, 5, 10),
////                1800.00,
////                5.0
////        );
////
//////        // Cadastrando
////        vendedorDAO.cadastrarVendedor(vendedor);
////
////       System.out.println(
////                "ID gerado: " + vendedor.getVendedorID()
////        );
//
//        // Buscando pelo ID
//        Vendedor encontrado =
//                vendedorDAO.buscarPorID(2);
//
//        if (encontrado != null) {
//
//            System.out.println(
//                    "Nome: " + encontrado.getNomeVendedor()
//            );
//
//            System.out.println(
//                    "Salário base: " + encontrado.getSalarioBase()
//            );
//
//            System.out.println(
//                    "Salário final: " + encontrado.calculaSalario()
//            );


