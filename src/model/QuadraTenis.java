package model;

import util.Formatador;
import util.Validador;

/**
 * [POO - HERANÇA] Quadra de tênis: preço depende do tipo de piso e da cobertura.
 */
public class QuadraTenis extends Quadra {

    public static final double TAXA_COBERTURA_HORA = 25.0;

    private TipoPiso tipoPiso;

    public QuadraTenis(String nome, double valorHoraBase, boolean coberta, TipoPiso tipoPiso) {
        super(nome, valorHoraBase, coberta);
        setTipoPiso(tipoPiso);
    }

    public QuadraTenis(String nome, double valorHoraBase, TipoPiso tipoPiso) {
        super(nome, valorHoraBase);
        setTipoPiso(tipoPiso);
    }

    // [POO - POLIMORFISMO / SOBRESCRITA] regra de preço específica do tênis
    @Override
    public double calcularPreco(int quantidadeHoras) {
        validarHoras(quantidadeHoras);
        double valor = getValorHoraBase() * quantidadeHoras * tipoPiso.getFatorPreco();
        if (isCoberta()) {
            valor += TAXA_COBERTURA_HORA * quantidadeHoras;
        }
        return Formatador.arredondarMoeda(valor);
    }

    @Override
    public String getTipo() {
        return "Tênis";
    }

    @Override
    protected String getDetalhesEspecificos() {
        return "Piso: " + tipoPiso;
    }

    public TipoPiso getTipoPiso() {
        return tipoPiso;
    }

    public void setTipoPiso(TipoPiso tipoPiso) {
        this.tipoPiso = Validador.validarNaoNulo(tipoPiso, "Tipo de piso");
    }
}
