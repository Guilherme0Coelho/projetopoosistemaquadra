package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Encapsula toda a leitura do console.
 *
 * SOLUÇÃO DO BUG CLÁSSICO DO SCANNER: nunca usamos nextInt()/nextDouble().
 * Toda leitura é feita com nextLine() e convertida manualmente (Integer.parseInt),
 * então nenhuma quebra de linha "sobra" no buffer.
 */
public final class ConsoleUtil {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String LINHA = "=".repeat(60);

    private ConsoleUtil() {
    }

    private static String lerLinha() {
        if (!SCANNER.hasNextLine()) {
            System.out.println("\nEntrada encerrada. Finalizando o sistema.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    public static String lerTexto(String rotulo) {
        while (true) {
            System.out.print(rotulo + ": ");
            String entrada = lerLinha();
            if (!entrada.isEmpty()) {
                return entrada;
            }
            imprimirErro("Campo obrigatório. Tente novamente.");
        }
    }

    public static String lerTextoOpcional(String rotulo) {
        System.out.print(rotulo + ": ");
        return lerLinha();
    }

    public static int lerInteiro(String rotulo) {
        while (true) {
            String entrada = lerTexto(rotulo);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                imprimirErro("Digite um número inteiro válido.");
            }
        }
    }

    // [POO - SOBRECARGA] versão com intervalo permitido
    public static int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            int valor = lerInteiro(rotulo);
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            imprimirErro("Informe um valor entre " + minimo + " e " + maximo + ".");
        }
    }

    public static double lerDecimal(String rotulo) {
        while (true) {
            String entrada = lerTexto(rotulo).replace(',', '.');
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                imprimirErro("Digite um número válido (ex: 89,90).");
            }
        }
    }

    public static LocalDate lerData(String rotulo) {
        while (true) {
            String entrada = lerTexto(rotulo + " (dd/MM/aaaa)");
            try {
                return Formatador.converterData(entrada);
            } catch (DateTimeParseException e) {
                imprimirErro("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    public static boolean lerSimNao(String pergunta) {
        while (true) {
            String entrada = lerTexto(pergunta + " (S/N)").toUpperCase();
            if (entrada.equals("S") || entrada.equals("SIM")) {
                return true;
            }
            if (entrada.equals("N") || entrada.equals("NAO") || entrada.equals("NÃO")) {
                return false;
            }
            imprimirErro("Responda S ou N.");
        }
    }

    public static void pausar() {
        System.out.print("\nPressione ENTER para continuar...");
        lerLinha();
    }

    public static void imprimirTitulo(String titulo) {
        System.out.println("\n" + LINHA);
        System.out.println("  " + titulo);
        System.out.println(LINHA);
    }

    public static void imprimirSucesso(String mensagem) {
        System.out.println("[OK] " + mensagem);
    }

    public static void imprimirErro(String mensagem) {
        System.out.println("[ERRO] " + mensagem);
    }

    public static void imprimirAviso(String mensagem) {
        System.out.println("[AVISO] " + mensagem);
    }
}
