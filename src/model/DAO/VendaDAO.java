package model.DAO;

import model.Entities.Venda;
import java.util.List;

public interface VendaDAO {

    public void inserirVenda(Venda venda);
    public void atualizarVenda(Venda venda);
    public void excluirVenda(Venda venda);
    public Venda buscarVenda(Venda venda);
    public List<Venda> listarVendas();
}
