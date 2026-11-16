package repository;

import java.util.Optional;
import model.Cliente;

public class ClienteRepositorio extends RepositorioMemoria<Cliente> {

    public Optional<Cliente> buscarPorCpf(String cpfSomenteDigitos) {
        return filtrar(cliente -> cliente.getCpf().equals(cpfSomenteDigitos)).stream().findFirst();
    }

    public boolean existeCpf(String cpfSomenteDigitos) {
        return buscarPorCpf(cpfSomenteDigitos).isPresent();
    }
}
