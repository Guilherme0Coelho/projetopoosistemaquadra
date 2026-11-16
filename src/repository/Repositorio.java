package repository;

import java.util.List;
import java.util.Optional;
import model.EntidadeBase;

/**
 * [POO - INTERFACE + GENERICS] Contrato de persistência para qualquer entidade.
 * Trocar a implementação (memória -> arquivo -> banco) não afeta a camada service.
 */
public interface Repositorio<T extends EntidadeBase> {

    T salvar(T entidade);

    Optional<T> buscarPorId(int id);

    List<T> listarTodos();

    boolean remover(int id);

    int contar();
}
