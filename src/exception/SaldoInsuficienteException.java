package exception;

import util.Formatador;

/**
 * Exceção CHECKED lançada quando o cliente não possui saldo para pagar a reserva.
 * Carrega dados extras (saldo e valor) para a camada de apresentação exibir detalhes.
 */
public class SaldoInsuficienteException extends Exception {

    private static final long serialVersionUID = 1L;

    private final double saldoAtual;
    private final double valorNecessario;

    public SaldoInsuficienteException(double saldoAtual, double valorNecessario) {
        super(String.format("Saldo insuficiente. Saldo atual: %s | Valor necessário: %s",
                Formatador.moeda(saldoAtual), Formatador.moeda(valorNecessario)));
        this.saldoAtual = saldoAtual;
        this.valorNecessario = valorNecessario;
    }

    public double getSaldoAtual() {
        return saldoAtual;
    }

    public double getValorNecessario() {
        return valorNecessario;
    }

    public double getValorFaltante() {
        return Formatador.arredondarMoeda(valorNecessario - saldoAtual);
    }
}
