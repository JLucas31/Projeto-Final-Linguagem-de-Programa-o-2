package model.Entities;

import java.time.LocalDate;
import java.time.Period;

public class Cliente {

    private int clientID;
    private String nomeCliente;
    private String CPF;
    private String email;
    private LocalDate dataNasc;

    public Cliente(String nomeCliente, String CPF, String email, LocalDate dataNasc) {
        this.nomeCliente = nomeCliente;
        this.CPF = CPF;
        this.email = email;
        this.dataNasc = dataNasc;
    }

    public Cliente() {
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
    }

    public boolean ehMaiorIdade() {

        if (dataNasc == null) {
            return false;
        }

        return Period.between(dataNasc, LocalDate.now())
                .getYears() >= 18;
    }


    public boolean validarCpf() {

        // Remove pontos, traços e outros caracteres
        String cpf = this.CPF.replaceAll("[^0-9]", "");

        // CPF precisa ter exatamente 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os números são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int soma = 0;

        // Calcula o primeiro dígito verificador
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }

        int resto = soma % 11;

        int primeiroDigito = (resto < 2) ? 0 : 11 - resto;

        if (primeiroDigito !=
                Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        soma = 0;

        // Calcula o segundo dígito verificador
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }

        resto = soma % 11;

        int segundoDigito = (resto < 2) ? 0 : 11 - resto;

        return segundoDigito ==
                Character.getNumericValue(cpf.charAt(10));
    }

}
