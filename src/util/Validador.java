package util;

import exception.DadoInvalidoException;
import java.util.regex.Pattern;

/**
 * Validações reutilizadas pelos construtores/setters das entidades.
 * Centraliza regras e evita duplicação (DRY).
 */
public final class Validador {

    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() {
    }

    public static String validarTextoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadoInvalidoException("O campo '" + campo + "' é obrigatório.");
        }
        return valor.trim();
    }

    // [POO - SOBRECARGA DE MÉTODO] mesmo nome, assinatura diferente
    public static String validarTextoObrigatorio(String valor, String campo, int tamanhoMinimo) {
        String texto = validarTextoObrigatorio(valor, campo);
        if (texto.length() < tamanhoMinimo) {
            throw new DadoInvalidoException(
                    "O campo '" + campo + "' deve ter no mínimo " + tamanhoMinimo + " caracteres.");
        }
        return texto;
    }

    /** Valida CPF (formato + dígitos verificadores) e retorna somente os dígitos. */
    public static String validarCpf(String cpf) {
        String digitos = validarTextoObrigatorio(cpf, "CPF").replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            throw new DadoInvalidoException("CPF inválido: deve conter 11 dígitos (não todos iguais).");
        }
        if (calcularDigitoCpf(digitos, 9) != digitos.charAt(9) - '0'
                || calcularDigitoCpf(digitos, 10) != digitos.charAt(10) - '0') {
            throw new DadoInvalidoException("CPF inválido: dígitos verificadores não conferem.");
        }
        return digitos;
    }

    private static int calcularDigitoCpf(String cpf, int quantidadeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidadeDigitos + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    public static String validarEmail(String email) {
        String texto = validarTextoObrigatorio(email, "E-mail").toLowerCase();
        if (!PADRAO_EMAIL.matcher(texto).matches()) {
            throw new DadoInvalidoException("E-mail inválido: " + email);
        }
        return texto;
    }

    public static String validarTelefone(String telefone) {
        String digitos = validarTextoObrigatorio(telefone, "Telefone").replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new DadoInvalidoException("Telefone inválido: informe DDD + número (10 ou 11 dígitos).");
        }
        return digitos;
    }

    public static double validarValorPositivo(double valor, String campo) {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor <= 0) {
            throw new DadoInvalidoException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }

    public static int validarInteiroPositivo(int valor, String campo) {
        if (valor <= 0) {
            throw new DadoInvalidoException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }

    public static <T> T validarNaoNulo(T objeto, String campo) {
        if (objeto == null) {
            throw new DadoInvalidoException("O campo '" + campo + "' é obrigatório.");
        }
        return objeto;
    }
}
