package service;

import exception.EntidadeNaoEncontradaException;
import exception.RegraNegocioException;
import java.util.List;
import model.Equipamento;
import repository.EquipamentoRepositorio;

public class EquipamentoService {

    private final EquipamentoRepositorio equipamentoRepositorio;

    public EquipamentoService(EquipamentoRepositorio equipamentoRepositorio) {
        this.equipamentoRepositorio = equipamentoRepositorio;
    }

    public Equipamento cadastrar(String descricao, double valorHora, int estoqueInicial) {
        return equipamentoRepositorio.salvar(new Equipamento(descricao, valorHora, estoqueInicial));
    }

    public Equipamento buscarPorId(int id) throws EntidadeNaoEncontradaException {
        return equipamentoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Equipamento", id));
    }

    public List<Equipamento> listarTodos() {
        return equipamentoRepositorio.listarTodos();
    }

    public List<Equipamento> listarDisponiveis() {
        return equipamentoRepositorio.listarDisponiveis();
    }

    public Equipamento reporEstoque(int id, int quantidade) throws EntidadeNaoEncontradaException {
        Equipamento equipamento = buscarPorId(id);
        equipamento.adicionarEstoque(quantidade);
        return equipamento;
    }

    public void remover(int id) throws EntidadeNaoEncontradaException {
        Equipamento equipamento = buscarPorId(id);
        if (equipamento.possuiUnidadesEmUso()) {
            throw new RegraNegocioException("Equipamento possui unidades alugadas em reservas ativas.");
        }
        equipamentoRepositorio.remover(id);
    }
}
