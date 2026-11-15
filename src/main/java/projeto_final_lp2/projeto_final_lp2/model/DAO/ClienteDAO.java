package projeto_final_lp2.projeto_final_lp2.model.DAO;

import projeto_final_lp2.projeto_final_lp2.model.Entities.Cliente;
import projeto_final_lp2.projeto_final_lp2.model.Entities.Venda;
import java.util.List;

public interface ClienteDAO {
    public void cadastrarCliente(Cliente cliente);
    public void atualizarCliente(Cliente cliente);
    public Cliente buscarPorCpf(String cpf);
    public List<Venda> listarCompras(int clienteId);
}
