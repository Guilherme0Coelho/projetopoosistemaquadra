package model;

import util.Formatador;

/**
 * [POO - INTERFACE] Contrato de aplicação de desconto. Implementada pela hierarquia Cliente.
 * Contém um método default (Java 8+): comportamento padrão herdado por quem implementa.
 */
public interface Descontavel {

    double aplicarDesconto(double valorBruto);

    double getPercentualDesconto();

    default String descreverDesconto() {
        return getPercentualDesconto() == 0
                ? "sem desconto"
                : Formatador.percentual(getPercentualDesconto()) + " de desconto";
    }
}
