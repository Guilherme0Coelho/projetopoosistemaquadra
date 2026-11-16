package model;

/**
 * Tipos de piso de quadra de tênis com fator multiplicador de preço.
 */
public enum TipoPiso {

    SAIBRO("Saibro", 1.00),
    RAPIDA("Rápida (Hard)", 1.15),
    GRAMA("Grama sintética", 1.30);

    private final String descricao;
    private final double fatorPreco;

    TipoPiso(String descricao, double fatorPreco) {
        this.descricao = descricao;
        this.fatorPreco = fatorPreco;
    }

    public double getFatorPreco() {
        return fatorPreco;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
