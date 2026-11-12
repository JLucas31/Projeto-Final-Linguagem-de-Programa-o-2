package model.DAO;

import model.Entities.Cliente;
import model.Entities.Venda;
import java.util.List;

public interface ClienteDAO {
    public void cadastrarCliente(Cliente cliente);
    public void atualizarCliente(Cliente cliente);
    public Cliente buscarPorCpf(String cpf);
    public List<Venda> listarCompras(int clienteId);
}
