package model;

/**
 * Planos de sócio: cada constante carrega seus próprios dados (enum com atributos).
 */
public enum PlanoSocio {

    BRONZE("Bronze", 0.10, 3, 99.90),
    PRATA("Prata", 0.20, 4, 149.90),
    OURO("Ouro", 0.30, 5, 199.90);

    private final String descricao;
    private final double percentualDesconto;
    private final int limiteReservasAtivas;
    private final double mensalidade;

    PlanoSocio(String descricao, double percentualDesconto, int limiteReservasAtivas, double mensalidade) {
        this.descricao = descricao;
        this.percentualDesconto = percentualDesconto;
        this.limiteReservasAtivas = limiteReservasAtivas;
        this.mensalidade = mensalidade;
    }

    public double getPercentualDesconto() {
        return percentualDesconto;
    }

    public int getLimiteReservasAtivas() {
        return limiteReservasAtivas;
    }

    public double getMensalidade() {
        return mensalidade;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
