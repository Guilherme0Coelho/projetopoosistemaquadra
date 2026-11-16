package model;

/**
 * Ciclo de vida de uma reserva: PENDENTE -> CONFIRMADA -> (CANCELADA | CONCLUIDA).
 */
public enum StatusReserva {

    PENDENTE("Pendente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusReserva(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
