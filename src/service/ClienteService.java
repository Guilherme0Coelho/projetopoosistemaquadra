package service;

import exception.EntidadeNaoEncontradaException;
import exception.RegraNegocioException;
import java.util.List;
import model.Cliente;
import model.ClienteAvulso;
import model.ClienteSocio;
import model.PlanoSocio;
import repository.ClienteRepositorio;
import repository.ReservaRepositorio;
import util.Validador;

/**
 * Regras de negócio de clientes. A camada de apresentação (Main) só conversa com services.
 */
public class ClienteService {

    private final ClienteRepositorio clienteRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    public ClienteService(ClienteRepositorio clienteRepositorio, ReservaRepositorio reservaRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    public Cliente cadastrarAvulso(String nome, String cpf, String email, String telefone) {
        return salvarNovo(new ClienteAvulso(nome, cpf, email, telefone));
    }

    public Cliente cadastrarSocio(String nome, String cpf, String email, String telefone, PlanoSocio plano) {
        return salvarNovo(new ClienteSocio(nome, cpf, email, telefone, plano));
    }

    // [POO - POLIMORFISMO] recebe qualquer subtipo de Cliente
    private Cliente salvarNovo(Cliente cliente) {
        if (clienteRepositorio.existeCpf(cliente.getCpf())) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com este CPF.");
        }
        return clienteRepositorio.salvar(cliente);
    }

    public Cliente buscarPorId(int id) throws EntidadeNaoEncontradaException {
        return clienteRepositorio.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
    }

    public Cliente buscarPorCpf(String cpf) throws EntidadeNaoEncontradaException {
        String cpfNormalizado = Validador.validarCpf(cpf);
        return clienteRepositorio.buscarPorCpf(cpfNormalizado)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum cliente encontrado com o CPF informado."));
    }

    public List<Cliente> listarTodos() {
        return clienteRepositorio.listarTodos();
    }

    /** Campos em branco mantêm o valor atual. */
    public Cliente atualizarContato(int id, String novoEmail, String novoTelefone)
            throws EntidadeNaoEncontradaException {
        Cliente cliente = buscarPorId(id);
        if (novoEmail != null && !novoEmail.isBlank()) {
            cliente.setEmail(novoEmail);
        }
        if (novoTelefone != null && !novoTelefone.isBlank()) {
            cliente.setTelefone(novoTelefone);
        }
        return cliente;
    }

    public ClienteSocio alterarPlano(int id, PlanoSocio novoPlano) throws EntidadeNaoEncontradaException {
        Cliente cliente = buscarPorId(id);
        // Pattern matching para instanceof (Java 16+): verifica o tipo e já faz o cast
        if (!(cliente instanceof ClienteSocio socio)) {
            throw new RegraNegocioException("Apenas clientes sócios possuem plano.");
        }
        socio.setPlano(novoPlano);
        return socio;
    }

    public Cliente depositar(int id, double valor) throws EntidadeNaoEncontradaException {
        Cliente cliente = buscarPorId(id);
        cliente.depositar(valor);
        return cliente;
    }

    public void remover(int id) throws EntidadeNaoEncontradaException {
        Cliente cliente = buscarPorId(id);
        if (reservaRepositorio.contarAtivasPorCliente(cliente) > 0) {
            throw new RegraNegocioException("Cliente possui reservas ativas. Cancele-as antes de remover.");
        }
        clienteRepositorio.remover(id);
    }
}
