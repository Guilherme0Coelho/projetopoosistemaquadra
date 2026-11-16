package model;

/**
 * [POO - INTERFACE] Contrato: "quem é precificável sabe calcular seu preço por hora de uso".
 * Implementada por classes SEM relação de herança entre si: Quadra e Equipamento.
 */
public interface Precificavel {

    double calcularPreco(int horas);

    /** Nome amigável usado em tabelas de preço. */
    String getRotulo();
}
