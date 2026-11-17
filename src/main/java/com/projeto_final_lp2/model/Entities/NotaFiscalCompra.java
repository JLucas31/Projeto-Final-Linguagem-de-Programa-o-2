package com.projeto_final_lp2.model.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Nota fiscal de ENTRADA de mercadoria (compra de fornecedor).
// Substitui o antigo NotaFiscal.java (que tinha so a chave de acesso).
public class NotaFiscalCompra {

    private int nota_id;
    private String numero;
    private String chave_acesso;
    private String fornecedor;
    private LocalDate data_entrada;
    private double valor_total;
    private final ArrayList<ItemNota> itens = new ArrayList<>();

    public NotaFiscalCompra(int nota_id, String numero, String chave_acesso,
                            String fornecedor, LocalDate data_entrada) {
        this.nota_id = nota_id;
        setNumero(numero);
        setChave_acesso(chave_acesso);
        setFornecedor(fornecedor);
        setData_entrada(data_entrada);
        this.valor_total = 0; // calculado a partir dos itens
    }

    // Pode haver mais de uma linha do mesmo produto (lotes diferentes)
    public void adicionarItem(ItemNota item) {
        if (item == null) {
            throw new IllegalArgumentException("Item nao pode ser nulo");
        }
        itens.add(item);
        calcularTotal();
    }

    public void calcularTotal() {
        double soma = 0;
        for (ItemNota item : itens) {
            soma += item.calculaSubtotal();
        }
        this.valor_total = soma;
    }

    // ---------- Getters e setters ----------

    public int getNota_id() {
        return nota_id;
    }

    public void setNota_id(int nota_id) {
        this.nota_id = nota_id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Numero da nota invalido");
        }
        this.numero = numero;
    }

    public String getChave_acesso() {
        return chave_acesso;
    }

    public void setChave_acesso(String chave_acesso) {
        if (chave_acesso == null || chave_acesso.isBlank() || chave_acesso.length() > 44) {
            throw new IllegalArgumentException("Chave de acesso invalida (maximo 44 caracteres)");
        }
        this.chave_acesso = chave_acesso;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        if (fornecedor == null || fornecedor.isBlank()) {
            throw new IllegalArgumentException("Fornecedor invalido");
        }
        this.fornecedor = fornecedor;
    }

    public LocalDate getData_entrada() {
        return data_entrada;
    }

    public void setData_entrada(LocalDate data_entrada) {
        if (data_entrada == null) {
            throw new IllegalArgumentException("Data de entrada invalida");
        }
        this.data_entrada = data_entrada;
    }

    public double getValor_total() {
        return valor_total;
    }

    // Sem setValor_total: o total eh sempre calculado a partir dos itens.

    public List<ItemNota> getItens() {
        return Collections.unmodifiableList(itens);
    }
}