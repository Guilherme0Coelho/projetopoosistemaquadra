package model;

/**
 * Cliente que paga por uso, sem plano.
 * [POO - HERANÇA] "ClienteAvulso É UM Cliente".
 */
public class ClienteAvulso extends Cliente {

    private static final int LIMITE_RESERVAS_ATIVAS = 2;

    public ClienteAvulso(String nome, String cpf, String email, String telefone) {
        super(nome, cpf, email, telefone);
    }

    public ClienteAvulso(String nome, String cpf, String email) {
        super(nome, cpf, email);
    }

    // [POO - POLIMORFISMO / SOBRESCRITA] avulso não tem desconto
    @Override
    public double aplicarDesconto(double valorBruto) {
        return valorBruto;
    }

    @Override
    public double getPercentualDesconto() {
        return 0.0;
    }

    @Override
    public String getCategoria() {
        return "Avulso";
    }

    @Override
    public int getLimiteReservasAtivas() {
        return LIMITE_RESERVAS_ATIVAS;
    }
}
