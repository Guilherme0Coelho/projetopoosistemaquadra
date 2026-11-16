package model;

import exception.DadoInvalidoException;
import exception.RegraNegocioException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.Formatador;
import util.Validador;

/**
 * Reserva de uma quadra por um cliente, com equipamentos opcionais.
 *
 * [POO - AGREGAÇÃO] Reserva referencia Cliente e Quadra, que vivem independentemente dela.
 * [POO - COMPOSIÇÃO] Reserva É DONA da lista de ItemReserva (cria, controla e não a expõe para escrita).
 */
public class Reserva extends EntidadeBase {

    public static final int HORARIO_ABERTURA = 7;
    public static final int HORARIO_FECHAMENTO = 22;
    public static final int MAXIMO_HORAS = 3;
    public static final long HORAS_ANTECEDENCIA_REEMBOLSO_INTEGRAL = 24;
    public static final double PERCENTUAL_REEMBOLSO_TARDIO = 0.5;

    private final Cliente cliente;
    private final Quadra quadra;
    private final LocalDate data;
    private final int horaInicio;
    private final int quantidadeHoras;
    private final List<ItemReserva> itens = new ArrayList<>();
    private final LocalDateTime dataCriacao;
    private StatusReserva status;
    private double valorPago;
    private double valorReembolsado;

    public Reserva(Cliente cliente, Quadra quadra, LocalDate data, int horaInicio, int quantidadeHoras) {
        validarHorario(horaInicio, quantidadeHoras);
        this.cliente = Validador.validarNaoNulo(cliente, "Cliente");
        this.quadra = Validador.validarNaoNulo(quadra, "Quadra");
        this.data = Validador.validarNaoNulo(data, "Data");
        this.horaInicio = horaInicio;
        this.quantidadeHoras = quantidadeHoras;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusReserva.PENDENTE;
    }

    // [POO - SOBRECARGA DE CONSTRUTOR] reserva padrão de 1 hora
    public Reserva(Cliente cliente, Quadra quadra, LocalDate data, int horaInicio) {
        this(cliente, quadra, data, horaInicio, 1);
    }

    private static void validarHorario(int horaInicio, int quantidadeHoras) {
        if (quantidadeHoras < 1 || quantidadeHoras > MAXIMO_HORAS) {
            throw new DadoInvalidoException("A reserva deve ter entre 1 e " + MAXIMO_HORAS + " horas.");
        }
        if (horaInicio < HORARIO_ABERTURA || horaInicio + quantidadeHoras > HORARIO_FECHAMENTO) {
            throw new DadoInvalidoException(String.format("Horário fora do funcionamento da arena (%s às %s).",
                    Formatador.horario(HORARIO_ABERTURA), Formatador.horario(HORARIO_FECHAMENTO)));
        }
    }

    // ---------- Composição: gerenciamento dos itens ----------
    public void adicionarItem(Equipamento equipamento, int quantidade) {
        exigirStatus(StatusReserva.PENDENTE, "adicionar equipamentos a");
        for (ItemReserva item : itens) {
            if (item.getEquipamento().equals(equipamento)) {
                item.incrementarQuantidade(quantidade);
                return;
            }
        }
        itens.add(new ItemReserva(equipamento, quantidade));
    }

    /** [POO - ENCAPSULAMENTO] devolve visão somente-leitura; ninguém altera a lista por fora. */
    public List<ItemReserva> getItens() {
        return Collections.unmodifiableList(itens);
    }

    // ---------- Cálculos ----------
    public double calcularValorQuadra() {
        return quadra.calcularPreco(quantidadeHoras, horaInicio); // sobrecarga + polimorfismo
    }

    public double calcularValorEquipamentos() {
        return Formatador.arredondarMoeda(itens.stream()
                .mapToDouble(item -> item.calcularSubtotal(quantidadeHoras))
                .sum());
    }

    public double calcularValorBruto() {
        return Formatador.arredondarMoeda(calcularValorQuadra() + calcularValorEquipamentos());
    }

    /** [POO - POLIMORFISMO] aplicarDesconto() executa a versão de ClienteAvulso OU ClienteSocio. */
    public double calcularValorFinal() {
        return Formatador.arredondarMoeda(cliente.aplicarDesconto(calcularValorBruto()));
    }

    // ---------- Regras de agenda ----------
    public boolean conflitaCom(LocalDate outraData, int outroInicio, int outrasHoras) {
        if (status == StatusReserva.CANCELADA) {
            return false;
        }
        return data.equals(outraData)
                && outroInicio < getHoraFim()
                && (outroInicio + outrasHoras) > horaInicio;
    }

    public boolean ocupaHora(LocalDate outraData, int hora) {
        return conflitaCom(outraData, hora, 1);
    }

    // ---------- Máquina de estados ----------
    public void confirmar(double valorPago) {
        exigirStatus(StatusReserva.PENDENTE, "confirmar");
        this.valorPago = Validador.validarValorPositivo(valorPago, "Valor pago");
        this.status = StatusReserva.CONFIRMADA;
    }

    public double calcularValorReembolso(LocalDateTime momentoCancelamento) {
        long horasAntecedencia = Duration.between(momentoCancelamento, getDataHoraInicio()).toHours();
        return horasAntecedencia >= HORAS_ANTECEDENCIA_REEMBOLSO_INTEGRAL
                ? valorPago
                : Formatador.arredondarMoeda(valorPago * PERCENTUAL_REEMBOLSO_TARDIO);
    }

    /** Cancela e retorna o valor a ser reembolsado. */
    public double cancelar(LocalDateTime momentoCancelamento) {
        exigirStatus(StatusReserva.CONFIRMADA, "cancelar");
        if (!momentoCancelamento.isBefore(getDataHoraInicio())) {
            throw new RegraNegocioException("Não é possível cancelar uma reserva cujo horário já começou ou passou.");
        }
        this.valorReembolsado = calcularValorReembolso(momentoCancelamento);
        this.status = StatusReserva.CANCELADA;
        return valorReembolsado;
    }

    public void concluir() {
        exigirStatus(StatusReserva.CONFIRMADA, "concluir");
        this.status = StatusReserva.CONCLUIDA;
    }

    private void exigirStatus(StatusReserva esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException(String.format(
                    "Não é possível %s a reserva: status atual é '%s'.", acao, status));
        }
    }

    // ---------- Getters (sem setters: estado só muda pelas regras acima) ----------
    public Cliente getCliente() {
        return cliente;
    }

    public Quadra getQuadra() {
        return quadra;
    }

    public LocalDate getData() {
        return data;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public int getHoraFim() {
        return horaInicio + quantidadeHoras;
    }

    public int getQuantidadeHoras() {
        return quantidadeHoras;
    }

    public LocalDateTime getDataHoraInicio() {
        return LocalDateTime.of(data, LocalTime.of(horaInicio, 0));
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public boolean isAtiva() {
        return status == StatusReserva.CONFIRMADA;
    }

    public double getValorPago() {
        return valorPago;
    }

    public double getValorReembolsado() {
        return valorReembolsado;
    }

    /** Valor que efetivamente ficou com a arena (pago - reembolsado). */
    public double getValorRetido() {
        return Formatador.arredondarMoeda(valorPago - valorReembolsado);
    }

    @Override
    public String getDescricaoResumida() {
        return String.format("Reserva - %s em %s %s-%s (%s)", quadra.getNome(), Formatador.data(data),
                Formatador.horario(horaInicio), Formatador.horario(getHoraFim()), status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Reserva #%d [%s]%n", getId(), status));
        sb.append(String.format("  Cliente....: %s (%s)%n", cliente.getNome(), cliente.getCategoria()));
        sb.append(String.format("  Quadra.....: %s%n", quadra.getRotulo()));
        sb.append(String.format("  Data/Hora..: %s das %s às %s (%dh)%n", Formatador.data(data),
                Formatador.horario(horaInicio), Formatador.horario(getHoraFim()), quantidadeHoras));
        if (itens.isEmpty()) {
            sb.append(String.format("  Itens......: nenhum equipamento%n"));
        } else {
            for (ItemReserva item : itens) {
                sb.append(String.format("  Item.......: %s%n", item));
            }
        }
        sb.append(String.format("  Valor pago.: %s", Formatador.moeda(valorPago)));
        if (status == StatusReserva.CANCELADA) {
            sb.append(String.format(" | Reembolsado: %s", Formatador.moeda(valorReembolsado)));
        }
        return sb.toString();
    }
}
