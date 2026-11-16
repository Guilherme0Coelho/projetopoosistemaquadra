package model;

import exception.DadoInvalidoException;

/**
 * Superclasse abstrata RAIZ de todas as entidades persistidas.
 *
 * [POO - ABSTRAÇÃO] Não pode ser instanciada (abstract). Define o que TODA entidade tem
 * (um ID) e o que TODA entidade sabe fazer (getDescricaoResumida), sem dizer COMO.
 * [POO - POLIMORFISMO] Permite List<EntidadeBase> contendo Clientes, Quadras, Equipamentos e Reservas.
 */
public abstract class EntidadeBase {

    // [POO - ENCAPSULAMENTO] privado; só pode ser definido UMA vez via definirId()
    private Integer id;

    public Integer getId() {
        return id;
    }

    /** Chamado pelo repositório ao salvar. Impede troca de ID depois de atribuído. */
    public void definirId(int id) {
        if (this.id != null) {
            throw new DadoInvalidoException("O ID desta entidade já foi definido e não pode ser alterado.");
        }
        if (id <= 0) {
            throw new DadoInvalidoException("O ID deve ser positivo.");
        }
        this.id = id;
    }

    /** Método abstrato: cada subclasse é OBRIGADA a implementar a sua versão. */
    public abstract String getDescricaoResumida();

    // [POO - SOBRESCRITA] equals/hashCode baseados na identidade (ID)
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (outro == null || getClass() != outro.getClass()) {
            return false;
        }
        EntidadeBase entidade = (EntidadeBase) outro;
        return id != null && id.equals(entidade.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
