package exception;

/**
 * Lançada quando um construtor ou setter recebe um valor inválido.
 *
 * [POO - ENCAPSULAMENTO] É a "barreira" que impede setters burros:
 * nenhum objeto entra em estado inconsistente.
 */
public class DadoInvalidoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
