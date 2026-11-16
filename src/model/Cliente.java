package model;

import exception.SaldoInsuficienteException;
import java.util.Objects;
import util.Formatador;
import util.Validador;

/**
 * Superclasse abstrata dos clientes da arena.
 *
 * [POO - ABSTRAÇÃO/HERANÇA] Concentra atributos comuns (nome, cpf, e-mail, saldo) e deixa
 * abstratos os comportamentos que variam por categoria (desconto, limite de reservas).
 * [POO - INTERFACE] Implementa Descontavel, mas delega a implementação às subclasses.
 */
public abstract class Cliente extends EntidadeBase implements Descontavel {

    private static final String TELEFONE_NAO_INFORMADO = "Não informado";

    // [POO - ENCAPSULAMENTO] todos os atributos privados
    private String nome;
    private final String cpf; // imutável: CPF não muda após o cadastro
    private String email;
    private String telefone;
    private double saldo;     // sem setSaldo(): só muda via depositar()/debitar()

    protected Cliente(String nome, String cpf, String email, String telefone) {
        this.cpf = Validador.validarCpf(cpf);
        setNome(nome);
        setEmail(email);
        setTelefone(telefone);
    }

    // [POO - SOBRECARGA DE CONSTRUTOR] telefone opcional
    protected Cliente(String nome, String cpf, String email) {
        this(nome, cpf, email, null);
    }

    // ---------- Comportamentos que cada subclasse especializa ----------
    public abstract String getCategoria();

    public abstract int getLimiteReservasAtivas();

    // ---------- Regras de negócio da carteira ----------
    public void depositar(double valor) {
        Validador.validarValorPositivo(valor, "Valor do depósito");
        saldo = Formatador.arredondarMoeda(saldo + valor);
    }

    public void debitar(double valor) throws SaldoInsuficienteException {
        Validador.validarValorPositivo(valor, "Valor do débito");
        if (valor > saldo) {
            throw new SaldoInsuficienteException(saldo, valor);
        }
        saldo = Formatador.arredondarMoeda(saldo - valor);
    }

    // ---------- Getters / Setters com validação ----------
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validador.validarTextoObrigatorio(nome, "Nome", 3);
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validador.validarEmail(email);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = (telefone == null || telefone.isBlank())
                ? TELEFONE_NAO_INFORMADO
                : Validador.validarTelefone(telefone);
    }

    public double getSaldo() {
        return saldo;
    }

    // ---------- Sobrescritas ----------
    @Override
    public String getDescricaoResumida() {
        return String.format("Cliente %s - %s (saldo %s)", getCategoria(), nome, Formatador.moeda(saldo));
    }

    @Override
    public String toString() {
        return String.format("#%d | %-6s | %-18s | CPF: %s | %s | Tel: %s | Saldo: %s",
                getId(), getCategoria(), nome, Formatador.cpf(cpf), email, telefone, Formatador.moeda(saldo));
    }

    /** [POO - SOBRESCRITA] Dois clientes são iguais se tiverem o mesmo CPF (chave natural). */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Cliente)) {
            return false;
        }
        return cpf.equals(((Cliente) outro).cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }
}
