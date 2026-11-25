package com.projeto_final_lp2.controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Filtro por coluna para as tabelas do sistema.
 *
 * Coloca um campo de busca embaixo do nome de cada coluna. Cada campo filtra
 * apenas a coluna em que está, e os filtros de várias colunas se combinam
 * (a linha precisa passar em todos).
 *
 * Regra do campo de busca:
 *   - as palavras são separadas por '+': "Poster+One+Piece";
 *   - a linha aparece se o texto da coluna contiver TODAS as palavras, em
 *     qualquer ordem (por isso "Poster+One+Piece" acha "Poster One Piece 50 x 50 cm"
 *     e outros posters de One Piece);
 *   - não diferencia maiúsculas de minúsculas nem acentos;
 *   - campo vazio não filtra nada; um espaço digitado faz parte da palavra
 *     ("One Piece" procura as duas palavras juntas, "One+Piece" procura as
 *     duas em qualquer posição).
 *
 * Uso (no initialize do controller):
 *
 *   filtro = new FiltroTabela<>(tabela, lista)
 *           .coluna(colNome, Produto::getNomeProduto)
 *           .coluna(colId, p -> String.valueOf(p.getProduto_id()));
 *
 * "lista" é a ObservableList com TODOS os registros: quando ela muda
 * (setAll), a tabela se atualiza respeitando os filtros digitados.
 */
public class FiltroTabela<T> {

    private final TableView<T> tabela;
    private final FilteredList<T> filtrada;

    // coluna -> (campo de busca, como obter o texto da coluna para um registro)
    private final Map<TableColumn<T, ?>, TextField> campos = new LinkedHashMap<>();
    private final Map<TableColumn<T, ?>, Function<T, String>> textos = new LinkedHashMap<>();

    public FiltroTabela(TableView<T> tabela, ObservableList<T> origem) {
        this.tabela = tabela;
        this.filtrada = new FilteredList<>(origem);

        // SortedList mantém a ordenação por clique no cabeçalho funcionando
        SortedList<T> ordenada = new SortedList<>(filtrada);
        ordenada.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenada);
    }

    /**
     * Cria o campo de busca embaixo do nome da coluna.
     *
     * @param coluna coluna da tabela (o nome vem do texto dela no FXML)
     * @param texto  texto que a coluna mostra para um registro; é nele que a busca procura
     */
    public FiltroTabela<T> coluna(TableColumn<T, ?> coluna, Function<T, String> texto) {
        String titulo = coluna.getText();

        Label rotulo = new Label(titulo);

        TextField campo = new TextField();
        campo.setPromptText("Buscar");
        // fonte normal: o cabeçalho da coluna usa negrito e o campo herdaria isso
        campo.setStyle("-fx-font-size: 11px; -fx-font-weight: normal;");
        campo.textProperty().addListener((obs, antigo, novo) -> atualizar());
        // o campo acompanha a largura da coluna
        campo.prefWidthProperty().bind(coluna.widthProperty().subtract(24));
        campo.setMinWidth(30);

        VBox cabecalho = new VBox(4, rotulo, campo);
        cabecalho.setAlignment(Pos.CENTER);

        // texto nulo + graphic: o cabeçalho mostra o nome e o campo, um embaixo do outro
        coluna.setText(null);
        coluna.setGraphic(cabecalho);

        campos.put(coluna, campo);
        textos.put(coluna, texto);
        return this;
    }

    // Apaga todos os campos de busca (a tabela volta a mostrar tudo)
    public void limparFiltros() {
        for (TextField campo : campos.values()) {
            campo.clear();
        }
        atualizar();
    }

    // Quantos registros estão visíveis com os filtros atuais
    public int visiveis() {
        return filtrada.size();
    }

    private void atualizar() {
        // lambda nova a cada vez: o FilteredList só refiltra quando o predicado muda
        filtrada.setPredicate(registro -> passa(registro));
    }

    private boolean passa(T registro) {
        for (Map.Entry<TableColumn<T, ?>, TextField> entrada : campos.entrySet()) {
            List<String> palavras = palavras(entrada.getValue().getText());
            if (palavras.isEmpty()) {
                continue;
            }

            String texto = textos.get(entrada.getKey()).apply(registro);
            if (!contemTodas(texto, palavras)) {
                return false;
            }
        }
        return true;
    }

    // =====================================================================
    // Regra de busca (métodos estáticos: dá para testar sem abrir tela)
    // =====================================================================

    /**
     * Divide o que foi digitado nas palavras da busca, separadas por '+'.
     * Palavras vazias ("Poster++One", "+") são ignoradas.
     */
    static List<String> palavras(String digitado) {
        List<String> palavras = new ArrayList<>();
        if (digitado == null) {
            return palavras;
        }
        for (String parte : digitado.split("\\+")) {
            String palavra = normalizar(parte);
            if (!palavra.isEmpty()) {
                palavras.add(palavra);
            }
        }
        return palavras;
    }

    // true se o texto contém todas as palavras (já normalizadas), em qualquer ordem
    static boolean contemTodas(String texto, List<String> palavras) {
        String alvo = normalizar(texto);
        for (String palavra : palavras) {
            if (!alvo.contains(palavra)) {
                return false;
            }
        }
        return true;
    }

    // minúsculas, sem acentos e sem espaços nas pontas
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}
