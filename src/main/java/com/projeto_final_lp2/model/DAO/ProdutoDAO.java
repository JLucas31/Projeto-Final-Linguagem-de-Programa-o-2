package com.projeto_final_lp2.model.DAO;

import com.projeto_final_lp2.model.Entities.Produto;
import java.util.List;

public interface ProdutoDAO {

    // Grava o produto e preenche o produto_id gerado pelo MySQL.
    // O setor eh obrigatorio (coluna NOT NULL).
    void cadastrarProduto(Produto produto);

    // Atualiza nome, marca, valor, setor e quantidade. A ultima data de
    // compra NAO eh alterada aqui: quem muda ela eh a entrada de nota fiscal.
    void atualizarProduto(Produto produto);

    // Falha com mensagem clara se o produto ja foi usado em alguma venda
    // ou nota fiscal (nao da para apagar o historico).
    void removerProduto(int id);

    // Retorna null se nao existir
    Produto buscarPorID(int id);

    // Busca por parte do nome (ignora maiusculas/minusculas)
    List<Produto> buscarPorNome(String nome);

    // Busca por parte do nome da marca
    List<Produto> buscarPorMarca(String marca);

    // Busca pelo setor exato (ignora maiusculas/minusculas)
    List<Produto> buscarPorSetor(String setor);

    List<Produto> listarProdutos();

    // Produtos com quantidade menor ou igual ao limite informado
    List<Produto> listarEstoqueBaixo(int limite);

    // Subtrai do estoque de uma vez so no banco. Falha se nao houver
    // quantidade suficiente (o estoque nunca fica negativo).
    void baixarEstoque(int id, int qtd);

    // Soma ao estoque (ajuste manual: nao mexe na ultima data de compra)
    void adicionarEstoque(int id, int qtd);
}
