package model.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Vendedor {

    private int vendedorID;
    private String nomeVendedor;
    private String telefone;
    private LocalDate dataNasc;
    private double salarioBase;
    private double percentualComissao;
    private double totalComissao;
    private ArrayList<Integer> vendas;

    // Construtor vazio
    public Vendedor() {
        this.vendas = new ArrayList<>();
    }

    // Construtor com os atributos principais
    public Vendedor(String nomeVendedor, String telefone,
                    LocalDate dataNasc, double salarioBase,
                    double percentualComissao) {

        this.nomeVendedor = nomeVendedor;
        this.telefone = telefone;
        this.dataNasc = dataNasc;
        this.salarioBase = salarioBase;
        this.percentualComissao = percentualComissao;
        this.totalComissao = 0;
        this.vendas = new ArrayList<>();
    }

    // Calcula a comissão sobre o total vendido
    public double calculaComissao(double totalVendido) {

        this.totalComissao =
                totalVendido * (percentualComissao / 100);

        return totalComissao;
    }

    // Calcula o salário junto com a comissão
    public double calculaSalario() {

        return salarioBase + totalComissao;
    }

    // Adiciona o ID de uma venda à lista do vendedor
    public void adicionarVenda(int vendaID) {

        if (vendaID <= 0) {
            throw new IllegalArgumentException(
                    "ID da venda inválido");
        }

        if (!vendas.contains(vendaID)) {
            vendas.add(vendaID);
        }
    }

    public int getVendedorID() {
        return vendedorID;
    }

    public void setVendedorID(int vendedorID) {
        this.vendedorID = vendedorID;
    }

    public String getNomeVendedor() {
        return nomeVendedor;
    }

    public void setNomeVendedor(String nomeVendedor) {
        this.nomeVendedor = nomeVendedor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
    }

    public double getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(double salarioBase) {
        this.salarioBase = salarioBase;
    }

    public double getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(double percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public double getTotalComissao() {
        return totalComissao;
    }

    public void setTotalComissao(double totalComissao) {
        this.totalComissao = totalComissao;
    }

    public List<Integer> getVendas() {
        return new ArrayList<>(vendas);
    }
}
