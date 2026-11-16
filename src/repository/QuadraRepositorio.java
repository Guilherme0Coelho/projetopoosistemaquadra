package repository;

import java.util.List;
import model.Quadra;

public class QuadraRepositorio extends RepositorioMemoria<Quadra> {

    public List<Quadra> listarAtivas() {
        return filtrar(Quadra::isAtiva);
    }
}
