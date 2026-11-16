package repository;

import java.util.List;
import model.Equipamento;

public class EquipamentoRepositorio extends RepositorioMemoria<Equipamento> {

    public List<Equipamento> listarDisponiveis() {
        return filtrar(equipamento -> equipamento.getEstoqueDisponivel() > 0);
    }
}
