package exception;

/**
 * Exceção base (unchecked) para violações de regras de negócio.
 *
 * [POO - EXCEÇÃO PERSONALIZADA + HERANÇA]
 * Estende RuntimeException: não obriga "throws" em cada assinatura, ideal para
 * validações de dados e regras que podem ocorrer em qualquer camada.
 * Serve de superclasse para DadoInvalidoException e HorarioIndisponivelException.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
