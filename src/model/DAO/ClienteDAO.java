package model.DAO;

import model.Entities.Cliente;
import model.Entities.Venda;
import java.util.List;

public interface ClienteDAO {
    public void cadastrarCliente(Cliente cliente);
    public void atualizarCliente();
    public Cliente buscarPorCpf(String cpf);
    public boolean validarCpf();
    public boolean ehMaiorIdade();
    public List<Venda> listarCompras();
}
