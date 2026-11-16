package model;

import util.Formatador;

/**
 * [POO - HERANÇA] Quadra de beach tennis: preço depende da cobertura e de areia premium.
 */
public class QuadraBeachTennis extends Quadra {

    public static final double TAXA_COBERTURA_HORA = 15.0;
    public static final double TAXA_AREIA_PREMIUM_HORA = 10.0;

    private boolean areiaPremium;

    public QuadraBeachTennis(String nome, double valorHoraBase, boolean coberta, boolean areiaPremium) {
        super(nome, valorHoraBase, coberta);
        this.areiaPremium = areiaPremium;
    }

    public QuadraBeachTennis(String nome, double valorHoraBase) {
        super(nome, valorHoraBase);
        this.areiaPremium = false;
    }

    // [POO - POLIMORFISMO / SOBRESCRITA] regra de preço específica do beach tennis
    @Override
    public double calcularPreco(int quantidadeHoras) {
        validarHoras(quantidadeHoras);
        double valor = getValorHoraBase() * quantidadeHoras;
        if (isCoberta()) {
            valor += TAXA_COBERTURA_HORA * quantidadeHoras;
        }
        if (areiaPremium) {
            valor += TAXA_AREIA_PREMIUM_HORA * quantidadeHoras;
        }
        return Formatador.arredondarMoeda(valor);
    }

    @Override
    public String getTipo() {
        return "Beach Tennis";
    }

    @Override
    protected String getDetalhesEspecificos() {
        return "Areia premium: " + (areiaPremium ? "Sim" : "Não");
    }

    public boolean isAreiaPremium() {
        return areiaPremium;
    }

    public void setAreiaPremium(boolean areiaPremium) {
        this.areiaPremium = areiaPremium;
    }
}
