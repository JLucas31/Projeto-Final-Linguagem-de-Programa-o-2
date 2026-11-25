package com.projeto_final_lp2.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Funcoes pequenas compartilhadas pelas telas (alertas, formatacao, leitura de campos)
public final class Util {

    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Util() {
    }

    public static String moeda(double valor) {
        return String.format(Locale.US, "R$ %.2f", valor).replace('.', ',');
    }

    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    public static void alerta(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public static void erro(String cabecalho, String mensagem) {
        alerta(Alert.AlertType.ERROR, "Erro", cabecalho, mensagem);
    }

    public static void aviso(String cabecalho, String mensagem) {
        alerta(Alert.AlertType.WARNING, "Aviso", cabecalho, mensagem);
    }

    public static void info(String cabecalho, String mensagem) {
        alerta(Alert.AlertType.INFORMATION, "Informação", cabecalho, mensagem);
    }

    public static boolean confirmar(String titulo, String cabecalho, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Texto do campo sem espacos nas pontas ("" se estiver vazio)
    public static String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    // Celula de tabela formatada como "R$ 0,00"
    public static <S> TableCell<S, Double> celulaMoeda() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null ? null : moeda(valor));
            }
        };
    }

    // Celula de tabela formatada como dd/MM/yyyy
    public static <S> TableCell<S, LocalDate> celulaData() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null ? null : data(valor));
            }
        };
    }
}
