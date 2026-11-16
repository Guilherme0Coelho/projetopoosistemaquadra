package main;

import exception.EntidadeNaoEncontradaException;
import exception.HorarioIndisponivelException;
import exception.RegraNegocioException;
import exception.SaldoInsuficienteException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cliente;
import model.Descontavel;
import model.EntidadeBase;
import model.Equipamento;
import model.PlanoSocio;
import model.Precificavel;
import model.Quadra;
import model.Reserva;
import model.StatusReserva;
import model.TipoPiso;
import repository.ClienteRepositorio;
import repository.EquipamentoRepositorio;
import repository.QuadraRepositorio;
import repository.ReservaRepositorio;
import service.ClienteService;
import service.EquipamentoService;
import service.QuadraService;
import service.RelatorioService;
import service.ReservaService;
import util.ConsoleUtil;
import util.Formatador;

/**
 * Camada de apresentação (CLI). Não contém regra de negócio: apenas lê dados,
 * chama os services e exibe resultados. Todas as exceções são tratadas aqui.
 */
public class Main {

    /** Ação de menu que pode lançar exceções checked; permite centralizar o try-catch. */
    @FunctionalInterface
    private interface AcaoMenu {
        void executar() throws Exception;
    }

    private final ClienteService clienteService;
    private final QuadraService quadraService;
    private final EquipamentoService equipamentoService;
    private final ReservaService reservaService;
    private final RelatorioService relatorioService;

    /** Injeção de dependências manual: monta repositórios e services. */
    public Main() {
        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        QuadraRepositorio quadraRepositorio = new QuadraRepositorio();
        EquipamentoRepositorio equipamentoRepositorio = new EquipamentoRepositorio();
        ReservaRepositorio reservaRepositorio = new ReservaRepositorio();

        this.clienteService = new ClienteService(clienteRepositorio, reservaRepositorio);
        this.quadraService = new QuadraService(quadraRepositorio, reservaRepositorio);
        this.equipamentoService = new EquipamentoService(equipamentoRepositorio);
        this.reservaService = new ReservaService(reservaRepositorio, clienteService, quadraService, equipamentoService);
        this.relatorioService = new RelatorioService(clienteService, quadraService, equipamentoService, reservaService);
    }

    public static void main(String[] args) {
        Main sistema = new Main();
        sistema.popularDadosIniciais();
        sistema.executar();
    }

    // =====================================================================
    // CARGA INICIAL
    // =====================================================================

    private void popularDadosIniciais() {
        try {
            Cliente ana = clienteService.cadastrarSocio("Ana Souza", "529.982.247-25",
                    "ana.souza@email.com", "11987654321", PlanoSocio.OURO);
            Cliente bruno = clienteService.cadastrarSocio("Bruno Lima", "111.444.777-35",
                    "bruno.lima@email.com", "11912345678", PlanoSocio.PRATA);
            Cliente carla = clienteService.cadastrarAvulso("Carla Mendes", "123.456.789-09",
                    "carla.mendes@email.com", "21999998888");
            Cliente diego = clienteService.cadastrarAvulso("Diego Rocha", "935.411.347-80",
                    "diego.rocha@email.com", "");

            ana.depositar(500);
            bruno.depositar(300);
            carla.depositar(200);
            diego.depositar(30); // saldo baixo de propósito: demonstra SaldoInsuficienteException

            Quadra central = quadraService.cadastrarTenis("Central Saibro", 80, false, TipoPiso.SAIBRO);
            Quadra rapida = quadraService.cadastrarTenis("Rápida Coberta", 100, true, TipoPiso.RAPIDA);
            Quadra beach1 = quadraService.cadastrarBeachTennis("Arena Beach 1", 60, false, false);
            quadraService.cadastrarBeachTennis("Arena Beach Premium", 70, true, true);

            Equipamento raqueteTenis = equipamentoService.cadastrar("Raquete de Tênis", 15, 10);
            equipamentoService.cadastrar("Raquete de Beach Tennis", 12, 8);
            Equipamento bolinhas = equipamentoService.cadastrar("Tubo de Bolinhas", 8, 20);

            LocalDate amanha = LocalDate.now().plusDays(1);
            LocalDate depoisDeAmanha = LocalDate.now().plusDays(2);

            reservaService.criarReserva(ana.getId(), central.getId(), amanha, 9, 2,
                    Map.of(raqueteTenis.getId(), 2));
            reservaService.criarReserva(carla.getId(), beach1.getId(), amanha, 19, 1);
            reservaService.criarReserva(bruno.getId(), rapida.getId(), depoisDeAmanha, 18, 2,
                    Map.of(bolinhas.getId(), 1));

            ConsoleUtil.imprimirSucesso(String.format(
                    "Dados iniciais carregados: %d clientes, %d quadras, %d equipamentos, %d reservas.",
                    clienteService.listarTodos().size(), quadraService.listarTodas().size(),
                    equipamentoService.listarTodos().size(), reservaService.listarTodas().size()));
        } catch (EntidadeNaoEncontradaException | SaldoInsuficienteException | RegraNegocioException e) {
            ConsoleUtil.imprimirErro("Falha na carga inicial: " + e.getMessage());
        }
    }

    // =====================================================================
    // LOOP PRINCIPAL + TRATAMENTO CENTRALIZADO DE EXCEÇÕES
    // =====================================================================

    private void executar() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("ARENA SMASH - SISTEMA DE RESERVA DE QUADRAS");
            System.out.println("1 - Clientes");
            System.out.println("2 - Quadras");
            System.out.println("3 - Equipamentos");
            System.out.println("4 - Reservas");
            System.out.println("5 - Relatórios (demonstração de polimorfismo)");
            System.out.println("0 - Sair");
            opcao = ConsoleUtil.lerInteiro("Escolha uma opção");
            switch (opcao) {
                case 1 -> menuClientes();
                case 2 -> menuQuadras();
                case 3 -> menuEquipamentos();
                case 4 -> menuReservas();
                case 5 -> menuRelatorios();
                case 0 -> System.out.println("Sistema encerrado. Até logo!");
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    /**
     * [EXCEÇÕES] Ponto único de tratamento: o programa nunca quebra por erro do usuário.
     * Ordem dos catch: do mais específico para o mais genérico.
     */
    private void executarComTratamento(AcaoMenu acao) {
        try {
            acao.executar();
        } catch (EntidadeNaoEncontradaException e) {
            ConsoleUtil.imprimirErro(e.getMessage());
        } catch (SaldoInsuficienteException e) {
            ConsoleUtil.imprimirErro(e.getMessage());
            ConsoleUtil.imprimirAviso("Faltam " + Formatador.moeda(e.getValorFaltante())
                    + ". Faça um depósito no menu Clientes.");
        } catch (HorarioIndisponivelException e) {
            ConsoleUtil.imprimirErro(e.getMessage());
            ConsoleUtil.imprimirAviso("Use 'Consultar horários livres' no menu Reservas.");
        } catch (RegraNegocioException e) { // também captura DadoInvalidoException (subclasse)
            ConsoleUtil.imprimirErro(e.getMessage());
        } catch (Exception e) {
            ConsoleUtil.imprimirErro("Erro inesperado: " + e.getMessage());
        }
        ConsoleUtil.pausar();
    }

    // =====================================================================
    // MENU CLIENTES
    // =====================================================================

    private void menuClientes() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("CLIENTES");
            System.out.println("1 - Cadastrar cliente avulso");
            System.out.println("2 - Cadastrar cliente sócio");
            System.out.println("3 - Listar clientes");
            System.out.println("4 - Buscar por CPF");
            System.out.println("5 - Atualizar contato");
            System.out.println("6 - Alterar plano de sócio");
            System.out.println("7 - Depositar saldo");
            System.out.println("8 - Remover cliente");
            System.out.println("0 - Voltar");
            opcao = ConsoleUtil.lerInteiro("Opção");
            switch (opcao) {
                case 1 -> executarComTratamento(this::cadastrarClienteAvulso);
                case 2 -> executarComTratamento(this::cadastrarClienteSocio);
                case 3 -> executarComTratamento(this::listarClientes);
                case 4 -> executarComTratamento(this::buscarClientePorCpf);
                case 5 -> executarComTratamento(this::atualizarContatoCliente);
                case 6 -> executarComTratamento(this::alterarPlanoCliente);
                case 7 -> executarComTratamento(this::depositarSaldo);
                case 8 -> executarComTratamento(this::removerCliente);
                case 0 -> { }
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void cadastrarClienteAvulso() {
        String nome = ConsoleUtil.lerTexto("Nome");
        String cpf = ConsoleUtil.lerTexto("CPF");
        String email = ConsoleUtil.lerTexto("E-mail");
        String telefone = ConsoleUtil.lerTextoOpcional("Telefone com DDD (ENTER para pular)");
        Cliente cliente = clienteService.cadastrarAvulso(nome, cpf, email, telefone);
        ConsoleUtil.imprimirSucesso("Cliente cadastrado:\n" + cliente);
    }

    private void cadastrarClienteSocio() {
        String nome = ConsoleUtil.lerTexto("Nome");
        String cpf = ConsoleUtil.lerTexto("CPF");
        String email = ConsoleUtil.lerTexto("E-mail");
        String telefone = ConsoleUtil.lerTextoOpcional("Telefone com DDD (ENTER para pular)");
        PlanoSocio plano = escolherOpcao("Plano", PlanoSocio.values());
        Cliente cliente = clienteService.cadastrarSocio(nome, cpf, email, telefone, plano);
        ConsoleUtil.imprimirSucesso("Sócio cadastrado:\n" + cliente);
    }

    private void listarClientes() {
        imprimirLista("Clientes", clienteService.listarTodos());
    }

    private void buscarClientePorCpf() throws EntidadeNaoEncontradaException {
        String cpf = ConsoleUtil.lerTexto("CPF");
        System.out.println(clienteService.buscarPorCpf(cpf));
    }

    private void atualizarContatoCliente() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do cliente");
        System.out.println("Atual: " + clienteService.buscarPorId(id));
        String email = ConsoleUtil.lerTextoOpcional("Novo e-mail (ENTER mantém)");
        String telefone = ConsoleUtil.lerTextoOpcional("Novo telefone (ENTER mantém)");
        Cliente atualizado = clienteService.atualizarContato(id, email, telefone);
        ConsoleUtil.imprimirSucesso("Contato atualizado:\n" + atualizado);
    }

    private void alterarPlanoCliente() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do sócio");
        PlanoSocio plano = escolherOpcao("Novo plano", PlanoSocio.values());
        ConsoleUtil.imprimirSucesso("Plano alterado:\n" + clienteService.alterarPlano(id, plano));
    }

    private void depositarSaldo() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do cliente");
        double valor = ConsoleUtil.lerDecimal("Valor do depósito");
        Cliente cliente = clienteService.depositar(id, valor);
        ConsoleUtil.imprimirSucesso("Novo saldo de " + cliente.getNome() + ": " + Formatador.moeda(cliente.getSaldo()));
    }

    private void removerCliente() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do cliente");
        Cliente cliente = clienteService.buscarPorId(id);
        if (ConsoleUtil.lerSimNao("Confirma a remoção de " + cliente.getNome() + "?")) {
            clienteService.remover(id);
            ConsoleUtil.imprimirSucesso("Cliente removido.");
        }
    }

    // =====================================================================
    // MENU QUADRAS
    // =====================================================================

    private void menuQuadras() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("QUADRAS");
            System.out.println("1 - Cadastrar quadra de tênis");
            System.out.println("2 - Cadastrar quadra de beach tennis");
            System.out.println("3 - Listar quadras");
            System.out.println("4 - Alterar valor da hora");
            System.out.println("5 - Ativar/Desativar quadra");
            System.out.println("6 - Remover quadra");
            System.out.println("0 - Voltar");
            opcao = ConsoleUtil.lerInteiro("Opção");
            switch (opcao) {
                case 1 -> executarComTratamento(this::cadastrarQuadraTenis);
                case 2 -> executarComTratamento(this::cadastrarQuadraBeach);
                case 3 -> executarComTratamento(this::listarQuadras);
                case 4 -> executarComTratamento(this::alterarValorHoraQuadra);
                case 5 -> executarComTratamento(this::alternarStatusQuadra);
                case 6 -> executarComTratamento(this::removerQuadra);
                case 0 -> { }
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void cadastrarQuadraTenis() {
        String nome = ConsoleUtil.lerTexto("Nome da quadra");
        double valor = ConsoleUtil.lerDecimal("Valor base da hora");
        boolean coberta = ConsoleUtil.lerSimNao("É coberta?");
        TipoPiso piso = escolherOpcao("Tipo de piso", TipoPiso.values());
        ConsoleUtil.imprimirSucesso("Quadra cadastrada:\n" + quadraService.cadastrarTenis(nome, valor, coberta, piso));
    }

    private void cadastrarQuadraBeach() {
        String nome = ConsoleUtil.lerTexto("Nome da quadra");
        double valor = ConsoleUtil.lerDecimal("Valor base da hora");
        boolean coberta = ConsoleUtil.lerSimNao("É coberta?");
        boolean premium = ConsoleUtil.lerSimNao("Possui areia premium?");
        ConsoleUtil.imprimirSucesso("Quadra cadastrada:\n"
                + quadraService.cadastrarBeachTennis(nome, valor, coberta, premium));
    }

    private void listarQuadras() {
        imprimirLista("Quadras", quadraService.listarTodas());
    }

    private void alterarValorHoraQuadra() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da quadra");
        double valor = ConsoleUtil.lerDecimal("Novo valor base da hora");
        ConsoleUtil.imprimirSucesso("Valor alterado:\n" + quadraService.alterarValorHora(id, valor));
    }

    private void alternarStatusQuadra() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da quadra");
        Quadra quadra = quadraService.alternarStatus(id);
        ConsoleUtil.imprimirSucesso("Quadra agora está " + (quadra.isAtiva() ? "ATIVA" : "INATIVA") + ".");
    }

    private void removerQuadra() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da quadra");
        Quadra quadra = quadraService.buscarPorId(id);
        if (ConsoleUtil.lerSimNao("Confirma a remoção de '" + quadra.getNome() + "'?")) {
            quadraService.remover(id);
            ConsoleUtil.imprimirSucesso("Quadra removida.");
        }
    }

    // =====================================================================
    // MENU EQUIPAMENTOS
    // =====================================================================

    private void menuEquipamentos() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("EQUIPAMENTOS");
            System.out.println("1 - Cadastrar equipamento");
            System.out.println("2 - Listar equipamentos");
            System.out.println("3 - Repor estoque");
            System.out.println("4 - Remover equipamento");
            System.out.println("0 - Voltar");
            opcao = ConsoleUtil.lerInteiro("Opção");
            switch (opcao) {
                case 1 -> executarComTratamento(this::cadastrarEquipamento);
                case 2 -> executarComTratamento(this::listarEquipamentos);
                case 3 -> executarComTratamento(this::reporEstoque);
                case 4 -> executarComTratamento(this::removerEquipamento);
                case 0 -> { }
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void cadastrarEquipamento() {
        String descricao = ConsoleUtil.lerTexto("Descrição");
        double valor = ConsoleUtil.lerDecimal("Valor da hora (por unidade)");
        int estoque = ConsoleUtil.lerInteiro("Estoque inicial");
        ConsoleUtil.imprimirSucesso("Equipamento cadastrado:\n" + equipamentoService.cadastrar(descricao, valor, estoque));
    }

    private void listarEquipamentos() {
        imprimirLista("Equipamentos", equipamentoService.listarTodos());
    }

    private void reporEstoque() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do equipamento");
        int quantidade = ConsoleUtil.lerInteiro("Quantidade a adicionar");
        ConsoleUtil.imprimirSucesso("Estoque atualizado:\n" + equipamentoService.reporEstoque(id, quantidade));
    }

    private void removerEquipamento() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID do equipamento");
        Equipamento equipamento = equipamentoService.buscarPorId(id);
        if (ConsoleUtil.lerSimNao("Confirma a remoção de '" + equipamento.getDescricao() + "'?")) {
            equipamentoService.remover(id);
            ConsoleUtil.imprimirSucesso("Equipamento removido.");
        }
    }

    // =====================================================================
    // MENU RESERVAS
    // =====================================================================

    private void menuReservas() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("RESERVAS");
            System.out.println("1 - Nova reserva");
            System.out.println("2 - Consultar horários livres");
            System.out.println("3 - Listar todas as reservas");
            System.out.println("4 - Listar reservas de um cliente");
            System.out.println("5 - Detalhar reserva");
            System.out.println("6 - Cancelar reserva");
            System.out.println("7 - Concluir reserva");
            System.out.println("0 - Voltar");
            opcao = ConsoleUtil.lerInteiro("Opção");
            switch (opcao) {
                case 1 -> executarComTratamento(this::novaReserva);
                case 2 -> executarComTratamento(this::consultarHorariosLivres);
                case 3 -> executarComTratamento(this::listarReservas);
                case 4 -> executarComTratamento(this::listarReservasPorCliente);
                case 5 -> executarComTratamento(this::detalharReserva);
                case 6 -> executarComTratamento(this::cancelarReserva);
                case 7 -> executarComTratamento(this::concluirReserva);
                case 0 -> { }
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void novaReserva() throws EntidadeNaoEncontradaException, SaldoInsuficienteException {
        imprimirListaResumida(clienteService.listarTodos());
        int clienteId = ConsoleUtil.lerInteiro("ID do cliente");
        imprimirListaResumida(quadraService.listarAtivas());
        int quadraId = ConsoleUtil.lerInteiro("ID da quadra");
        LocalDate data = ConsoleUtil.lerData("Data");
        imprimirHorariosLivres(quadraId, data);
        int hora = ConsoleUtil.lerInteiro("Hora de início (7 a 21)",
                Reserva.HORARIO_ABERTURA, Reserva.HORARIO_FECHAMENTO - 1);
        int horas = ConsoleUtil.lerInteiro("Quantidade de horas (1 a " + Reserva.MAXIMO_HORAS + ")",
                1, Reserva.MAXIMO_HORAS);
        Map<Integer, Integer> equipamentos = lerEquipamentosDesejados();

        Reserva orcamento = reservaService.simularReserva(clienteId, quadraId, data, hora, horas, equipamentos);
        imprimirOrcamento(orcamento);

        if (ConsoleUtil.lerSimNao("Confirmar reserva e debitar do saldo?")) {
            Reserva reserva = reservaService.criarReserva(clienteId, quadraId, data, hora, horas, equipamentos);
            ConsoleUtil.imprimirSucesso("Reserva confirmada!\n" + reserva);
        } else {
            ConsoleUtil.imprimirAviso("Reserva descartada.");
        }
    }

    private Map<Integer, Integer> lerEquipamentosDesejados() {
        Map<Integer, Integer> equipamentos = new LinkedHashMap<>();
        while (ConsoleUtil.lerSimNao("Deseja alugar equipamento?")) {
            imprimirListaResumida(equipamentoService.listarDisponiveis());
            int id = ConsoleUtil.lerInteiro("ID do equipamento");
            int quantidade = ConsoleUtil.lerInteiro("Quantidade", 1, 50);
            equipamentos.merge(id, quantidade, Integer::sum);
        }
        return equipamentos;
    }

    private void imprimirOrcamento(Reserva reserva) {
        Cliente cliente = reserva.getCliente();
        ConsoleUtil.imprimirTitulo("ORÇAMENTO");
        System.out.println("Quadra.........: " + Formatador.moeda(reserva.calcularValorQuadra())
                + (reserva.getHoraFim() > Quadra.HORA_INICIO_NOTURNO ? " (inclui acréscimo noturno)" : ""));
        System.out.println("Equipamentos...: " + Formatador.moeda(reserva.calcularValorEquipamentos()));
        System.out.println("Valor bruto....: " + Formatador.moeda(reserva.calcularValorBruto()));
        System.out.println("Desconto.......: " + cliente.getCategoria() + " - " + cliente.descreverDesconto());
        System.out.println("VALOR FINAL....: " + Formatador.moeda(reserva.calcularValorFinal()));
        System.out.println("Saldo cliente..: " + Formatador.moeda(cliente.getSaldo()));
    }

    private void consultarHorariosLivres() throws EntidadeNaoEncontradaException {
        imprimirListaResumida(quadraService.listarAtivas());
        int quadraId = ConsoleUtil.lerInteiro("ID da quadra");
        LocalDate data = ConsoleUtil.lerData("Data");
        imprimirHorariosLivres(quadraId, data);
    }

    private void imprimirHorariosLivres(int quadraId, LocalDate data) throws EntidadeNaoEncontradaException {
        List<Integer> livres = reservaService.listarHorariosLivres(quadraId, data);
        if (livres.isEmpty()) {
            ConsoleUtil.imprimirAviso("Nenhum horário livre nesta data.");
            return;
        }
        StringBuilder sb = new StringBuilder("Horários livres em " + Formatador.data(data) + ": ");
        livres.forEach(hora -> sb.append(Formatador.horario(hora)).append("  "));
        System.out.println(sb.toString().trim());
    }

    private void listarReservas() {
        imprimirListaDetalhada("Reservas", reservaService.listarTodas());
    }

    private void listarReservasPorCliente() throws EntidadeNaoEncontradaException {
        int clienteId = ConsoleUtil.lerInteiro("ID do cliente");
        imprimirListaDetalhada("Reservas do cliente", reservaService.listarPorCliente(clienteId));
    }

    private void detalharReserva() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da reserva");
        System.out.println(reservaService.buscarPorId(id));
    }

    private void cancelarReserva() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da reserva");
        System.out.println(reservaService.buscarPorId(id));
        ConsoleUtil.imprimirAviso("Cancelamento com menos de " + Reserva.HORAS_ANTECEDENCIA_REEMBOLSO_INTEGRAL
                + "h de antecedência reembolsa apenas " + Formatador.percentual(Reserva.PERCENTUAL_REEMBOLSO_TARDIO) + ".");
        if (ConsoleUtil.lerSimNao("Confirma o cancelamento?")) {
            double reembolso = reservaService.cancelarReserva(id);
            ConsoleUtil.imprimirSucesso("Reserva cancelada. Reembolso creditado: " + Formatador.moeda(reembolso));
        }
    }

    private void concluirReserva() throws EntidadeNaoEncontradaException {
        int id = ConsoleUtil.lerInteiro("ID da reserva");
        Reserva reserva = reservaService.concluirReserva(id);
        ConsoleUtil.imprimirSucesso("Reserva #" + reserva.getId() + " concluída. Equipamentos devolvidos ao estoque.");
    }

    // =====================================================================
    // MENU RELATÓRIOS (vitrine de POLIMORFISMO para a apresentação)
    // =====================================================================

    private void menuRelatorios() {
        int opcao;
        do {
            ConsoleUtil.imprimirTitulo("RELATÓRIOS");
            System.out.println("1 - Visão geral do sistema (List<EntidadeBase>)");
            System.out.println("2 - Tabela de preços (List<Precificavel>)");
            System.out.println("3 - Simular descontos por cliente (Descontavel)");
            System.out.println("4 - Faturamento e reservas por status");
            System.out.println("0 - Voltar");
            opcao = ConsoleUtil.lerInteiro("Opção");
            switch (opcao) {
                case 1 -> executarComTratamento(this::relatorioVisaoGeral);
                case 2 -> executarComTratamento(this::relatorioTabelaPrecos);
                case 3 -> executarComTratamento(this::relatorioSimulacaoDescontos);
                case 4 -> executarComTratamento(this::relatorioFaturamento);
                case 0 -> { }
                default -> ConsoleUtil.imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    /** [POO - POLIMORFISMO] Mesmo método, comportamento decidido pelo tipo REAL do objeto. */
    private void relatorioVisaoGeral() {
        List<EntidadeBase> entidades = relatorioService.listarTodasEntidades();
        for (EntidadeBase entidade : entidades) {
            System.out.printf("%-18s -> %s%n", entidade.getClass().getSimpleName(), entidade.getDescricaoResumida());
        }
        System.out.println("Total de objetos: " + entidades.size());
    }

    /** [POO - INTERFACE] Quadras e Equipamentos tratados como Precificavel. */
    private void relatorioTabelaPrecos() {
        int horas = ConsoleUtil.lerInteiro("Simular para quantas horas (1 a 3)", 1, Reserva.MAXIMO_HORAS);
        System.out.printf("%n%-45s %s%n", "ITEM", "PREÇO (" + horas + "h, diurno)");
        for (Precificavel item : relatorioService.listarItensPrecificaveis()) {
            System.out.printf("%-45s %s%n", item.getRotulo(), Formatador.moeda(item.calcularPreco(horas)));
        }
    }

    /** [POO - POLIMORFISMO via interface] cada cliente aplica o próprio desconto. */
    private void relatorioSimulacaoDescontos() {
        double valor = ConsoleUtil.lerDecimal("Valor bruto para simular");
        for (Cliente cliente : clienteService.listarTodos()) {
            Descontavel descontavel = cliente; // referência pelo tipo da interface
            System.out.printf("%-18s %-7s %-16s -> %s%n", cliente.getNome(), cliente.getCategoria(),
                    "(" + descontavel.descreverDesconto() + ")",
                    Formatador.moeda(descontavel.aplicarDesconto(valor)));
        }
    }

    private void relatorioFaturamento() {
        System.out.println("Faturamento retido: " + Formatador.moeda(reservaService.calcularFaturamento()));
        for (Map.Entry<StatusReserva, Long> entrada : reservaService.contarPorStatus().entrySet()) {
            System.out.printf("  %-11s: %d%n", entrada.getKey(), entrada.getValue());
        }
    }

    // =====================================================================
    // HELPERS DE EXIBIÇÃO (genéricos)
    // =====================================================================

    private <E extends Enum<E>> E escolherOpcao(String titulo, E[] opcoes) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("  %d - %s%n", i + 1, opcoes[i]);
        }
        return opcoes[ConsoleUtil.lerInteiro("Opção", 1, opcoes.length) - 1];
    }

    /** [POO - POLIMORFISMO] toString() chamado é o da classe concreta de cada item. */
    private void imprimirLista(String titulo, List<? extends EntidadeBase> itens) {
        if (itens.isEmpty()) {
            ConsoleUtil.imprimirAviso("Nenhum registro de " + titulo.toLowerCase() + ".");
            return;
        }
        System.out.println("--- " + titulo + " (" + itens.size() + ") ---");
        itens.forEach(System.out::println);
    }

    private void imprimirListaDetalhada(String titulo, List<? extends EntidadeBase> itens) {
        if (itens.isEmpty()) {
            ConsoleUtil.imprimirAviso("Nenhum registro de " + titulo.toLowerCase() + ".");
            return;
        }
        for (EntidadeBase item : itens) {
            System.out.println(item);
            System.out.println("-".repeat(60));
        }
    }

    private void imprimirListaResumida(List<? extends EntidadeBase> itens) {
        for (EntidadeBase item : itens) {
            System.out.printf("  [%d] %s%n", item.getId(), item.getDescricaoResumida());
        }
    }
}
