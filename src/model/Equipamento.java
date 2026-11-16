package model;

import exception.RegraNegocioException;
import util.Formatador;
import util.Validador;

/**
 * Equipamento alugável (raquetes, bolinhas...).
 * [POO - INTERFACE] Implementa Precificavel, assim como Quadra, sem nenhuma herança em comum
 * além de EntidadeBase: o contrato é o que une as duas classes.
 */
public class Equipamento extends EntidadeBase implements Precificavel {

    private String descricao;
    private double valorHora;
    private int estoqueTotal;
    private int estoqueDisponivel;

    public Equipamento(String descricao, double valorHora, int estoqueInicial) {
        setDescricao(descricao);
        setValorHora(valorHora);
        this.estoqueTotal = Validador.validarInteiroPositivo(estoqueInicial, "Estoque inicial");
        this.estoqueDisponivel = estoqueInicial;
    }

    // [POO - SOBRECARGA DE CONSTRUTOR] estoque padrão de 1 unidade
    public Equipamento(String descricao, double valorHora) {
        this(descricao, valorHora, 1);
    }

    @Override
    public double calcularPreco(int horas) {
        Validador.validarInteiroPositivo(horas, "Horas");
        return Formatador.arredondarMoeda(valorHora * horas);
    }

    // [POO - SOBRECARGA DE MÉTODO] preço para N unidades
    public double calcularPreco(int horas, int quantidade) {
        Validador.validarInteiroPositivo(quantidade, "Quantidade");
        return Formatador.arredondarMoeda(calcularPreco(horas) * quantidade);
    }

    // ---------- Regras de estoque ----------
    public void retirar(int quantidade) {
        Validador.validarInteiroPositivo(quantidade, "Quantidade");
        if (quantidade > estoqueDisponivel) {
            throw new RegraNegocioException(String.format(
                    "Estoque insuficiente de '%s'. Disponível: %d.", descricao, estoqueDisponivel));
        }
        estoqueDisponivel -= quantidade;
    }

    public void devolver(int quantidade) {
        Validador.validarInteiroPositivo(quantidade, "Quantidade");
        if (estoqueDisponivel + quantidade > estoqueTotal) {
            throw new RegraNegocioException("Devolução excede o estoque total de '" + descricao + "'.");
        }
        estoqueDisponivel += quantidade;
    }

    public void adicionarEstoque(int quantidade) {
        Validador.validarInteiroPositivo(quantidade, "Quantidade");
        estoqueTotal += quantidade;
        estoqueDisponivel += quantidade;
    }

    public boolean possuiUnidadesEmUso() {
        return estoqueDisponivel < estoqueTotal;
    }

    // ---------- Getters / Setters ----------
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = Validador.validarTextoObrigatorio(descricao, "Descrição", 3);
    }

    public double getValorHora() {
        return valorHora;
    }

    public void setValorHora(double valorHora) {
        this.valorHora = Validador.validarValorPositivo(valorHora, "Valor da hora");
    }

    public int getEstoqueTotal() {
        return estoqueTotal;
    }

    public int getEstoqueDisponivel() {
        return estoqueDisponivel;
    }

    @Override
    public String getRotulo() {
        return "Equipamento - " + descricao;
    }

    @Override
    public String getDescricaoResumida() {
        return String.format("Equipamento - %s (%d/%d disponíveis)", descricao, estoqueDisponivel, estoqueTotal);
    }

    @Override
    public String toString() {
        return String.format("#%d | %-24s | %s/h | Estoque: %d disponível(is) de %d",
                getId(), descricao, Formatador.moeda(valorHora), estoqueDisponivel, estoqueTotal);
    }
}
