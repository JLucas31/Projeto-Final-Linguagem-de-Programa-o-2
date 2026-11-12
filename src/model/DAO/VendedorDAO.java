package model.DAO;

import model.Entities.Vendedor;
import java.util.List;

public interface VendedorDAO {

    void cadastrarVendedor(Vendedor vendedor);

    Vendedor buscarPorID(int id);

    List<Vendedor> listarVendedores();

    double atualizarComissao(int vendedorID);
}
