package service;

import java.util.ArrayList;
import java.util.List;
import model.EntidadeBase;
import model.Precificavel;

/**
 * Reúne objetos de tipos diferentes em coleções da superclasse/interface,
 * para demonstrar POLIMORFISMO em tempo de execução.
 */
public class RelatorioService {

    private final ClienteService clienteService;
    private final QuadraService quadraService;
    private final EquipamentoService equipamentoService;
    private final ReservaService reservaService;

    public RelatorioService(ClienteService clienteService, QuadraService quadraService,
                            EquipamentoService equipamentoService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.quadraService = quadraService;
        this.equipamentoService = equipamentoService;
        this.reservaService = reservaService;
    }

    /** [POO - POLIMORFISMO] Lista heterogênea tipada pela superclasse abstrata. */
    public List<EntidadeBase> listarTodasEntidades() {
        List<EntidadeBase> entidades = new ArrayList<>();
        entidades.addAll(clienteService.listarTodos());
        entidades.addAll(quadraService.listarTodas());
        entidades.addAll(equipamentoService.listarTodos());
        entidades.addAll(reservaService.listarTodas());
        return entidades;
    }

    /** [POO - INTERFACE + POLIMORFISMO] Quadras e Equipamentos tratados pelo mesmo contrato. */
    public List<Precificavel> listarItensPrecificaveis() {
        List<Precificavel> itens = new ArrayList<>();
        itens.addAll(quadraService.listarAtivas());
        itens.addAll(equipamentoService.listarTodos());
        return itens;
    }
}
