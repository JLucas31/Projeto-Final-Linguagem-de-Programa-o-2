package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.NotaFiscalCompra;
import java.time.LocalDate;
import java.util.List;

// Os itens da nota (ItemNota) sao gravados e lidos junto com a nota:
// eles nao existem sozinhos, por isso nao tem DAO proprio.
public interface NotaFiscalCompraDAO {

    // Grava a nota e os itens, aumenta o estoque dos produtos e
    // atualiza a ultima data de compra de cada um
    void registrarEntrada(NotaFiscalCompra nota);

    NotaFiscalCompra buscarPorID(int id);

    NotaFiscalCompra buscarPorNumero(String numero);

    List<NotaFiscalCompra> listarPorPeriodo(LocalDate inicio, LocalDate fim);
}
