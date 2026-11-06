package model.Entities;

public class PagamentoCredito extends Pagamento {

    public static final int MAX_PARCELAS = 12;
    public static final int MAX_PARCELAS_SEM_JUROS = 10;
    public static final double TAXA_JUROS_MENSAL = 0.02; // 2% ao mes (ajuste como quiser)

    private String bandeira;
    private String ultimos_digitos;
    private int numero_parcelas;
    private double taxa_juros; // TAXA_JUROS_MENSAL que será cobrada acima de 10 parcelas

    public PagamentoCredito(double valor, String bandeira, String ultimos_digitos, int numero_parcelas) {
        super(valor);
        setBandeira(bandeira);
        setUltimos_digitos(ultimos_digitos);
        setNumero_parcelas(numero_parcelas);
    }

    public boolean validarParcelas() {
        return numero_parcelas >= 1 && numero_parcelas <= MAX_PARCELAS;
    }

    public boolean temJuros() {
        return taxa_juros > 0;
    }

    // Credito eh aceito se o parcelamento for valido (os dados do cartao
    // ja sao validados nos setters)

    @Override
    public boolean processar() {
        return validarParcelas();
    }

    // Ate 10x: valor da venda. 11x e 12x: valor com juros compostos
    @Override
    public double calcularValorFinal() {
        if (!temJuros()) {
            return getValor();
        }
        double total = getValor() * Math.pow(1 + taxa_juros, numero_parcelas);
        return arredondar(total);
    }

    public double calcularValorParcela() {
        return arredondar(calcularValorFinal() / numero_parcelas);
    }

    private double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        if (bandeira == null || bandeira.isBlank()) {
            throw new IllegalArgumentException("Bandeira invalida");
        }
        this.bandeira = bandeira;
    }

    public String getUltimos_digitos() {
        return ultimos_digitos;
    }

    public void setUltimos_digitos(String ultimos_digitos) {
        if (ultimos_digitos == null || !ultimos_digitos.matches("\\d{4}")) {
            throw new IllegalArgumentException("Informe os 4 ultimos digitos do cartao");
        }
        this.ultimos_digitos = ultimos_digitos;
    }

    public int getNumero_parcelas() {
        return numero_parcelas;
    }

    public void setNumero_parcelas(int numero_parcelas) {
        if (numero_parcelas < 1 || numero_parcelas > MAX_PARCELAS) {
            throw new IllegalArgumentException("Parcelas devem ser de 1 a " + MAX_PARCELAS);
        }
        this.numero_parcelas = numero_parcelas;
        this.taxa_juros = numero_parcelas > MAX_PARCELAS_SEM_JUROS ? TAXA_JUROS_MENSAL : 0;
    }

    public double getTaxa_juros() {
        return taxa_juros;
    }
}