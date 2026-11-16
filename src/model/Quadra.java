package model;

import exception.DadoInvalidoException;
import util.Formatador;
import util.Validador;

/**
 * Superclasse abstrata das quadras.
 *
 * [POO - ABSTRAÇÃO] Quadra genérica não existe: toda quadra é de Tênis ou de Beach Tennis.
 * [POO - INTERFACE] Implementa Precificavel, mas deixa calcularPreco(int) abstrato
 * (é implementado de forma diferente em cada subclasse).
 */
public abstract class Quadra extends EntidadeBase implements Precificavel {

    public static final int HORA_INICIO_NOTURNO = 18;
    public static final double ACRESCIMO_NOTURNO = 0.20;

    private String nome;
    private double valorHoraBase;
    private final boolean coberta;
    private boolean ativa;

    protected Quadra(String nome, double valorHoraBase, boolean coberta) {
        setNome(nome);
        setValorHoraBase(valorHoraBase);
        this.coberta = coberta;
        this.ativa = true;
    }

    // [POO - SOBRECARGA DE CONSTRUTOR] quadra descoberta por padrão
    protected Quadra(String nome, double valorHoraBase) {
        this(nome, valorHoraBase, false);
    }

    /** Cada tipo de quadra devolve sua descrição. */
    public abstract String getTipo();

    protected abstract String getDetalhesEspecificos();

    /**
     * [POO - SOBRECARGA DE MÉTODO] calcularPreco(horas) x calcularPreco(horas, horaInicio).
     * Esta versão chama a versão abstrata (polimórfica) e aplica acréscimo noturno.
     * É "final" para que nenhuma subclasse altere a regra do horário noturno (Template Method).
     */
    public final double calcularPreco(int quantidadeHoras, int horaInicio) {
        double valorBase = calcularPreco(quantidadeHoras); // chamada POLIMÓRFICA
        int horasNoturnas = Math.max(0, (horaInicio + quantidadeHoras) - Math.max(horaInicio, HORA_INICIO_NOTURNO));
        double acrescimo = (valorBase / quantidadeHoras) * horasNoturnas * ACRESCIMO_NOTURNO;
        return Formatador.arredondarMoeda(valorBase + acrescimo);
    }

    protected void validarHoras(int quantidadeHoras) {
        if (quantidadeHoras <= 0) {
            throw new DadoInvalidoException("A quantidade de horas deve ser maior que zero.");
        }
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validador.validarTextoObrigatorio(nome, "Nome da quadra", 3);
    }

    public double getValorHoraBase() {
        return valorHoraBase;
    }

    public void setValorHoraBase(double valorHoraBase) {
        this.valorHoraBase = Validador.validarValorPositivo(valorHoraBase, "Valor da hora");
    }

    public boolean isCoberta() {
        return coberta;
    }

    public boolean isAtiva() {
        return ativa;
    }

    @Override
    public String getRotulo() {
        return "Quadra " + getTipo() + " - " + nome;
    }

    @Override
    public String getDescricaoResumida() {
        return "Quadra " + getTipo() + " - " + nome + (ativa ? "" : " [INATIVA]");
    }

    // [POO - POLIMORFISMO] toString usa getTipo()/getDetalhesEspecificos(), resolvidos em tempo de execução
    @Override
    public String toString() {
        return String.format("#%d | %-12s | %-22s | %s/h | %-10s | %s | %s",
                getId(), getTipo(), nome, Formatador.moeda(valorHoraBase),
                coberta ? "Coberta" : "Descoberta", getDetalhesEspecificos(), ativa ? "ATIVA" : "INATIVA");
    }
}
