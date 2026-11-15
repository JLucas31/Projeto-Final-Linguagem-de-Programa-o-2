package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.Venda;
import java.util.List;

public interface VendaDAO {

    public void inserirVenda(Venda venda);
    public void adicionarItem(Venda venda);
    public void alterarPagamento(Venda venda);
    public void excluirVenda(Venda venda);
    public Venda buscarVenda(Venda venda);
    public List<Venda> listarVendas();
}
