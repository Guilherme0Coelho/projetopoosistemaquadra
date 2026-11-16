package repository;

import java.time.LocalDate;
import java.util.List;
import model.Cliente;
import model.Quadra;
import model.Reserva;

public class ReservaRepositorio extends RepositorioMemoria<Reserva> {

    public List<Reserva> listarPorCliente(Cliente cliente) {
        return filtrar(reserva -> reserva.getCliente().equals(cliente));
    }

    public List<Reserva> listarPorQuadraEData(Quadra quadra, LocalDate data) {
        return filtrar(reserva -> reserva.getQuadra().equals(quadra) && reserva.getData().equals(data));
    }

    public long contarAtivasPorCliente(Cliente cliente) {
        return filtrar(reserva -> reserva.getCliente().equals(cliente) && reserva.isAtiva()).size();
    }

    public boolean existeAtivaPorQuadra(Quadra quadra) {
        return !filtrar(reserva -> reserva.getQuadra().equals(quadra) && reserva.isAtiva()).isEmpty();
    }
}
