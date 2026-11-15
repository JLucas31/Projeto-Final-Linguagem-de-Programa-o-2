package projeto_final_lp2.projeto_final_lp2.model.DAO;

import projeto_final_lp2.projeto_final_lp2.model.Entities.Vendedor;
import java.util.List;

public interface VendedorDAO {

    void cadastrarVendedor(Vendedor vendedor);

    Vendedor buscarPorID(int id);

    List<Vendedor> listarVendedores();

    double atualizarComissao(int vendedorID);
}
