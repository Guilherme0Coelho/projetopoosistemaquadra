package model;

import java.util.Objects;
import util.Formatador;
import util.Validador;

/**
 * Item de uma reserva (equipamento + quantidade).
 *
 * [POO - COMPOSIÇÃO] O construtor é package-private: só a Reserva (mesmo pacote) cria itens.
 * O item não existe fora da reserva; se a reserva some, seus itens somem junto.
 * [POO - AGREGAÇÃO] O item apenas REFERENCIA um Equipamento, que existe independentemente.
 */
public class ItemReserva {

    private final Equipamento equipamento;
    private int quantidade;

    ItemReserva(Equipamento equipamento, int quantidade) {
        this.equipamento = Validador.validarNaoNulo(equipamento, "Equipamento");
        this.quantidade = Validador.validarInteiroPositivo(quantidade, "Quantidade");
    }

    void incrementarQuantidade(int adicional) {
        this.quantidade += Validador.validarInteiroPositivo(adicional, "Quantidade adicional");
    }

    public double calcularSubtotal(int horas) {
        return equipamento.calcularPreco(horas, quantidade);
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ItemReserva)) {
            return false;
        }
        return equipamento.equals(((ItemReserva) outro).equipamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipamento);
    }

    @Override
    public String toString() {
        return String.format("%dx %s (%s/h cada)",
                quantidade, equipamento.getDescricao(), Formatador.moeda(equipamento.getValorHora()));
    }
}
