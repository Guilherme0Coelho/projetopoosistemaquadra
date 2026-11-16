package model;

import java.time.LocalDate;
import util.Formatador;
import util.Validador;

/**
 * Cliente com plano mensal, que ganha desconto e maior limite de reservas.
 * [POO - HERANÇA] "ClienteSocio É UM Cliente" e acrescenta plano + data de adesão.
 */
public class ClienteSocio extends Cliente {

    private PlanoSocio plano;
    private final LocalDate dataAdesao;

    public ClienteSocio(String nome, String cpf, String email, String telefone, PlanoSocio plano) {
        super(nome, cpf, email, telefone); // [POO - HERANÇA] reaproveita validações da superclasse
        setPlano(plano);
        this.dataAdesao = LocalDate.now();
    }

    // [POO - SOBRECARGA DE CONSTRUTOR]
    public ClienteSocio(String nome, String cpf, String email, PlanoSocio plano) {
        this(nome, cpf, email, null, plano);
    }

    // [POO - POLIMORFISMO / SOBRESCRITA] desconto conforme o plano
    @Override
    public double aplicarDesconto(double valorBruto) {
        return Formatador.arredondarMoeda(valorBruto * (1 - plano.getPercentualDesconto()));
    }

    @Override
    public double getPercentualDesconto() {
        return plano.getPercentualDesconto();
    }

    @Override
    public String getCategoria() {
        return "Sócio";
    }

    @Override
    public int getLimiteReservasAtivas() {
        return plano.getLimiteReservasAtivas();
    }

    public PlanoSocio getPlano() {
        return plano;
    }

    public void setPlano(PlanoSocio plano) {
        this.plano = Validador.validarNaoNulo(plano, "Plano");
    }

    public LocalDate getDataAdesao() {
        return dataAdesao;
    }

    // [POO - SOBRESCRITA] reaproveita o toString da superclasse e complementa
    @Override
    public String toString() {
        return super.toString() + String.format(" | Plano: %s (%s, mensalidade %s)",
                plano, descreverDesconto(), Formatador.moeda(plano.getMensalidade()));
    }
}
