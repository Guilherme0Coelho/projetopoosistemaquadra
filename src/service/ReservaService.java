package service;

import exception.EntidadeNaoEncontradaException;
import exception.HorarioIndisponivelException;
import exception.RegraNegocioException;
import exception.SaldoInsuficienteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import model.Cliente;
import model.Equipamento;
import model.ItemReserva;
import model.Quadra;
import model.Reserva;
import model.StatusReserva;
import repository.ReservaRepositorio;

/**
 * Orquestra a regra de negócio principal: criar, cancelar e concluir reservas.
 * Cada validação fica em um método pequeno e nomeado (Clean Code).
 */
public class ReservaService {

    private final ReservaRepositorio reservaRepositorio;
    private final ClienteService clienteService;
    private final QuadraService quadraService;
    private final EquipamentoService equipamentoService;

    public ReservaService(ReservaRepositorio reservaRepositorio, ClienteService clienteService,
                          QuadraService quadraService, EquipamentoService equipamentoService) {
        this.reservaRepositorio = reservaRepositorio;
        this.clienteService = clienteService;
        this.quadraService = quadraService;
        this.equipamentoService = equipamentoService;
    }

    /** Monta e valida a reserva SEM salvar nem cobrar (usado para mostrar o orçamento). */
    public Reserva simularReserva(int clienteId, int quadraId, LocalDate data, int horaInicio, int horas,
                                  Map<Integer, Integer> equipamentos) throws EntidadeNaoEncontradaException {
        return montarReservaValidada(clienteId, quadraId, data, horaInicio, horas, equipamentos);
    }

    public Reserva criarReserva(int clienteId, int quadraId, LocalDate data, int horaInicio, int horas,
                                Map<Integer, Integer> equipamentos)
            throws EntidadeNaoEncontradaException, SaldoInsuficienteException {
        Reserva reserva = montarReservaValidada(clienteId, quadraId, data, horaInicio, horas, equipamentos);
        double valorFinal = reserva.calcularValorFinal();
        reserva.getCliente().debitar(valorFinal); // pode lançar SaldoInsuficienteException ANTES de alterar estado
        retirarEquipamentos(reserva);
        reserva.confirmar(valorFinal);
        return reservaRepositorio.salvar(reserva);
    }

    // [POO - SOBRECARGA DE MÉTODO] reserva sem equipamentos
    public Reserva criarReserva(int clienteId, int quadraId, LocalDate data, int horaInicio, int horas)
            throws EntidadeNaoEncontradaException, SaldoInsuficienteException {
        return criarReserva(clienteId, quadraId, data, horaInicio, horas, Map.of());
    }

    public double cancelarReserva(int reservaId) throws EntidadeNaoEncontradaException {
        Reserva reserva = buscarPorId(reservaId);
        double reembolso = reserva.cancelar(LocalDateTime.now());
        devolverEquipamentos(reserva);
        if (reembolso > 0) {
            reserva.getCliente().depositar(reembolso);
        }
        return reembolso;
    }

    public Reserva concluirReserva(int reservaId) throws EntidadeNaoEncontradaException {
        Reserva reserva = buscarPorId(reservaId);
        reserva.concluir();
        devolverEquipamentos(reserva);
        return reserva;
    }

    public Reserva buscarPorId(int id) throws EntidadeNaoEncontradaException {
        return reservaRepositorio.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Reserva", id));
    }

    public List<Reserva> listarTodas() {
        return reservaRepositorio.listarTodos();
    }

    public List<Reserva> listarPorCliente(int clienteId) throws EntidadeNaoEncontradaException {
        return reservaRepositorio.listarPorCliente(clienteService.buscarPorId(clienteId));
    }

    public List<Integer> listarHorariosLivres(int quadraId, LocalDate data) throws EntidadeNaoEncontradaException {
        Quadra quadra = quadraService.buscarPorId(quadraId);
        List<Reserva> reservasDoDia = reservaRepositorio.listarPorQuadraEData(quadra, data);
        List<Integer> livres = new ArrayList<>();
        for (int hora = Reserva.HORARIO_ABERTURA; hora < Reserva.HORARIO_FECHAMENTO; hora++) {
            if (!horarioJaPassou(data, hora) && !horaOcupada(reservasDoDia, data, hora)) {
                livres.add(hora);
            }
        }
        return livres;
    }

    public double calcularFaturamento() {
        return reservaRepositorio.listarTodos().stream()
                .mapToDouble(Reserva::getValorRetido)
                .sum();
    }

    public Map<StatusReserva, Long> contarPorStatus() {
        Map<StatusReserva, Long> contagem = new EnumMap<>(StatusReserva.class);
        for (StatusReserva status : StatusReserva.values()) {
            contagem.put(status, 0L);
        }
        reservaRepositorio.listarTodos().forEach(r -> contagem.merge(r.getStatus(), 1L, Long::sum));
        return contagem;
    }

    // ===================== Métodos privados de apoio =====================

    private Reserva montarReservaValidada(int clienteId, int quadraId, LocalDate data, int horaInicio,
                                          int horas, Map<Integer, Integer> equipamentos)
            throws EntidadeNaoEncontradaException {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        Quadra quadra = quadraService.buscarPorId(quadraId);
        validarQuadraAtiva(quadra);
        Reserva reserva = new Reserva(cliente, quadra, data, horaInicio, horas); // valida faixa de horário
        validarDataFutura(reserva);
        validarLimiteReservas(cliente);
        validarDisponibilidade(quadra, data, horaInicio, horas);
        adicionarEquipamentos(reserva, equipamentos);
        return reserva;
    }

    private void validarQuadraAtiva(Quadra quadra) {
        if (!quadra.isAtiva()) {
            throw new RegraNegocioException("A quadra '" + quadra.getNome() + "' está inativa.");
        }
    }

    private void validarDataFutura(Reserva reserva) {
        if (!reserva.getDataHoraInicio().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível reservar um horário que já passou.");
        }
    }

    private void validarLimiteReservas(Cliente cliente) {
        long ativas = reservaRepositorio.contarAtivasPorCliente(cliente);
        if (ativas >= cliente.getLimiteReservasAtivas()) { // limite POLIMÓRFICO (Avulso x Sócio)
            throw new RegraNegocioException(String.format(
                    "Cliente %s (%s) atingiu o limite de %d reservas ativas.",
                    cliente.getNome(), cliente.getCategoria(), cliente.getLimiteReservasAtivas()));
        }
    }

    private void validarDisponibilidade(Quadra quadra, LocalDate data, int horaInicio, int horas) {
        boolean conflito = reservaRepositorio.listarPorQuadraEData(quadra, data).stream()
                .anyMatch(r -> r.conflitaCom(data, horaInicio, horas));
        if (conflito) {
            throw new HorarioIndisponivelException(quadra.getNome(), data, horaInicio, horas);
        }
    }

    private void adicionarEquipamentos(Reserva reserva, Map<Integer, Integer> equipamentos)
            throws EntidadeNaoEncontradaException {
        for (Map.Entry<Integer, Integer> entrada : equipamentos.entrySet()) {
            Equipamento equipamento = equipamentoService.buscarPorId(entrada.getKey());
            int quantidade = entrada.getValue();
            if (quantidade > equipamento.getEstoqueDisponivel()) {
                throw new RegraNegocioException(String.format("Estoque insuficiente de '%s'. Disponível: %d.",
                        equipamento.getDescricao(), equipamento.getEstoqueDisponivel()));
            }
            reserva.adicionarItem(equipamento, quantidade);
        }
    }

    private void retirarEquipamentos(Reserva reserva) {
        for (ItemReserva item : reserva.getItens()) {
            item.getEquipamento().retirar(item.getQuantidade());
        }
    }

    private void devolverEquipamentos(Reserva reserva) {
        for (ItemReserva item : reserva.getItens()) {
            item.getEquipamento().devolver(item.getQuantidade());
        }
    }

    private boolean horaOcupada(List<Reserva> reservasDoDia, LocalDate data, int hora) {
        return reservasDoDia.stream().anyMatch(r -> r.ocupaHora(data, hora));
    }

    private boolean horarioJaPassou(LocalDate data, int hora) {
        return !LocalDateTime.of(data, LocalTime.of(hora, 0)).isAfter(LocalDateTime.now());
    }
}
