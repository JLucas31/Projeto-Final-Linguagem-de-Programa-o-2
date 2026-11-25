package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.Cotacao;
import com.projeto_final_lp2.model.Entities.StatusCotacao;
import java.util.List;

public interface CotacaoDAO {

    // Coloca o produto em cotacao: data de hoje, quantidade 0 e status
    // PENDENTE. Se o produto ja tem uma cotacao pendente, nao cria outra:
    // devolve a que ja existe (evita linhas repetidas na aba Cotacao).
    Cotacao solicitarCotacao(int produtoId);

    // Retorna null se nao existir
    Cotacao buscarPorID(int id);

    // Todas as cotacoes, as mais recentes primeiro
    List<Cotacao> listarCotacoes();

    // Define quanto vai ser comprado. So eh permitido na aba Cotacao e
    // enquanto a cotacao estiver pendente.
    void definirQuantidade(int idCotacao, int quantidade);

    // Marca como COMPRADO (exige quantidade maior que zero) ou volta
    // para PENDENTE.
    void atualizarStatus(int idCotacao, StatusCotacao status);

    void removerCotacao(int idCotacao);
}
