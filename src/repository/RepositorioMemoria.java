package repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import model.EntidadeBase;

/**
 * Implementação genérica em memória (HashMap) reaproveitada por todos os repositórios.
 * [POO - ABSTRAÇÃO + HERANÇA] classe abstrata: só existe para ser especializada.
 */
public abstract class RepositorioMemoria<T extends EntidadeBase> implements Repositorio<T> {

    private final Map<Integer, T> dados = new LinkedHashMap<>(); // mantém ordem de inserção
    private int proximoId = 1;

    @Override
    public T salvar(T entidade) {
        if (entidade.getId() == null) {
            entidade.definirId(proximoId++);
        }
        dados.put(entidade.getId(), entidade);
        return entidade;
    }

    @Override
    public Optional<T> buscarPorId(int id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<T> listarTodos() {
        return new ArrayList<>(dados.values()); // cópia defensiva
    }

    @Override
    public boolean remover(int id) {
        return dados.remove(id) != null;
    }

    @Override
    public int contar() {
        return dados.size();
    }

    /** Filtro reutilizável pelas subclasses. */
    protected List<T> filtrar(Predicate<T> criterio) {
        return dados.values().stream().filter(criterio).toList();
    }
}
