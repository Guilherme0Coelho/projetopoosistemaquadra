package service;

import exception.EntidadeNaoEncontradaException;
import exception.RegraNegocioException;
import java.util.List;
import model.Quadra;
import model.QuadraBeachTennis;
import model.QuadraTenis;
import model.TipoPiso;
import repository.QuadraRepositorio;
import repository.ReservaRepositorio;

public class QuadraService {

    private final QuadraRepositorio quadraRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    public QuadraService(QuadraRepositorio quadraRepositorio, ReservaRepositorio reservaRepositorio) {
        this.quadraRepositorio = quadraRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    public Quadra cadastrarTenis(String nome, double valorHora, boolean coberta, TipoPiso piso) {
        return quadraRepositorio.salvar(new QuadraTenis(nome, valorHora, coberta, piso));
    }

    public Quadra cadastrarBeachTennis(String nome, double valorHora, boolean coberta, boolean areiaPremium) {
        return quadraRepositorio.salvar(new QuadraBeachTennis(nome, valorHora, coberta, areiaPremium));
    }

    public Quadra buscarPorId(int id) throws EntidadeNaoEncontradaException {
        return quadraRepositorio.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Quadra", id));
    }

    public List<Quadra> listarTodas() {
        return quadraRepositorio.listarTodos();
    }

    public List<Quadra> listarAtivas() {
        return quadraRepositorio.listarAtivas();
    }

    public Quadra alterarValorHora(int id, double novoValor) throws EntidadeNaoEncontradaException {
        Quadra quadra = buscarPorId(id);
        quadra.setValorHoraBase(novoValor);
        return quadra;
    }

    /** Ativa ou desativa. Não permite desativar quadra com reservas ativas. */
    public Quadra alternarStatus(int id) throws EntidadeNaoEncontradaException {
        Quadra quadra = buscarPorId(id);
        if (quadra.isAtiva()) {
            garantirSemReservasAtivas(quadra, "desativar");
            quadra.desativar();
        } else {
            quadra.ativar();
        }
        return quadra;
    }

    public void remover(int id) throws EntidadeNaoEncontradaException {
        Quadra quadra = buscarPorId(id);
        garantirSemReservasAtivas(quadra, "remover");
        quadraRepositorio.remover(id);
    }

    private void garantirSemReservasAtivas(Quadra quadra, String acao) {
        if (reservaRepositorio.existeAtivaPorQuadra(quadra)) {
            throw new RegraNegocioException("Não é possível " + acao + " a quadra: há reservas ativas.");
        }
    }
}
