package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Utilitário de formatação (classe final + construtor privado = não instanciável).
 */
public final class Formatador {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private Formatador() {
    }

    public static String moeda(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    public static String percentual(double fracao) {
        return String.format(PT_BR, "%.0f%%", fracao * 100);
    }

    public static String data(LocalDate data) {
        return data.format(FORMATO_DATA);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora.format(FORMATO_DATA_HORA);
    }

    public static String horario(int hora) {
        return String.format("%02d:00", hora);
    }

    public static String cpf(String cpfSomenteDigitos) {
        return cpfSomenteDigitos.substring(0, 3) + "." + cpfSomenteDigitos.substring(3, 6) + "."
                + cpfSomenteDigitos.substring(6, 9) + "-" + cpfSomenteDigitos.substring(9);
    }

    /** Converte "dd/MM/aaaa" em LocalDate. Lança DateTimeParseException se inválida. */
    public static LocalDate converterData(String texto) {
        return LocalDate.parse(texto.trim(), FORMATO_DATA);
    }

    /** Arredonda para 2 casas, evitando resíduos de ponto flutuante (ex: 0.30000000004). */
    public static double arredondarMoeda(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
