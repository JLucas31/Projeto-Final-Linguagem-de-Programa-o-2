package model.Entities;

import java.time.LocalDate;

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
}
