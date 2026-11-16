package exception;

/**
 * Exceção CHECKED: o compilador obriga quem chama a tratar (try-catch) ou propagar (throws).
 *
 * [POO - EXCEÇÃO PERSONALIZADA + SOBRECARGA DE CONSTRUTORES]
 */
public class EntidadeNaoEncontradaException extends Exception {

    private static final long serialVersionUID = 1L;

    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

    public EntidadeNaoEncontradaException(String nomeEntidade, int id) {
        this(nomeEntidade + " com ID " + id + " não encontrado(a).");
    }
}
