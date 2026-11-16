package exception;

import java.time.LocalDate;
import util.Formatador;

/**
 * Lançada quando o horário solicitado conflita com outra reserva da mesma quadra.
 */
public class HorarioIndisponivelException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public HorarioIndisponivelException(String nomeQuadra, LocalDate data, int horaInicio, int quantidadeHoras) {
        super(String.format("A quadra '%s' já está reservada em %s entre %s e %s.",
                nomeQuadra,
                Formatador.data(data),
                Formatador.horario(horaInicio),
                Formatador.horario(horaInicio + quantidadeHoras)));
    }
}
