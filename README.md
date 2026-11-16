# Arena Smash — Sistema de Reserva de Quadras (Tênis / Beach Tennis)

Projeto acadêmico de POO em **Java 17 puro**, CLI, persistência em memória (Collections).

## Como executar

**Windows (duplo clique):** `executar.bat`

**Manual (qualquer SO):**
```bash
# Windows (cmd)
dir /s /b src\*.java > fontes.txt
# Linux/macOS
find src -name "*.java" > fontes.txt

javac -encoding UTF-8 -d out @fontes.txt
java -cp out main.Main
```
> Acentos quebrados no terminal do Windows: rode `chcp 65001` antes (o `.bat` já faz isso).

---

## 1. Diagrama de Classes

```mermaid
classDiagram
    direction TB

    class EntidadeBase {
        <<abstract>>
        -Integer id
        +getId() Integer
        +definirId(int) void
        +getDescricaoResumida()* String
        +equals(Object) boolean
        +hashCode() int
    }

    class Precificavel {
        <<interface>>
        +calcularPreco(int horas) double
        +getRotulo() String
    }

    class Descontavel {
        <<interface>>
        +aplicarDesconto(double) double
        +getPercentualDesconto() double
        +descreverDesconto() String
    }

    class Cliente {
        <<abstract>>
        -String nome
        -String cpf
        -String email
        -String telefone
        -double saldo
        +depositar(double) void
        +debitar(double) void
        +getCategoria()* String
        +getLimiteReservasAtivas()* int
    }
    class ClienteAvulso {
        +aplicarDesconto(double) double
    }
    class ClienteSocio {
        -PlanoSocio plano
        -LocalDate dataAdesao
        +aplicarDesconto(double) double
    }

    class Quadra {
        <<abstract>>
        -String nome
        -double valorHoraBase
        -boolean coberta
        -boolean ativa
        +calcularPreco(int)* double
        +calcularPreco(int, int horaInicio) double
        +getTipo()* String
    }
    class QuadraTenis {
        -TipoPiso tipoPiso
        +calcularPreco(int) double
    }
    class QuadraBeachTennis {
        -boolean areiaPremium
        +calcularPreco(int) double
    }

    class Equipamento {
        -String descricao
        -double valorHora
        -int estoqueTotal
        -int estoqueDisponivel
        +calcularPreco(int) double
        +calcularPreco(int, int qtd) double
        +retirar(int) void
        +devolver(int) void
    }

    class Reserva {
        -Cliente cliente
        -Quadra quadra
        -LocalDate data
        -int horaInicio
        -int quantidadeHoras
        -List~ItemReserva~ itens
        -StatusReserva status
        -double valorPago
        -double valorReembolsado
        +adicionarItem(Equipamento, int) void
        +calcularValorFinal() double
        +conflitaCom(LocalDate, int, int) boolean
        +confirmar(double) void
        +cancelar(LocalDateTime) double
        +concluir() void
    }
    class ItemReserva {
        -Equipamento equipamento
        -int quantidade
        +calcularSubtotal(int horas) double
    }

    class PlanoSocio { <<enumeration>> BRONZE PRATA OURO }
    class TipoPiso { <<enumeration>> SAIBRO RAPIDA GRAMA }
    class StatusReserva { <<enumeration>> PENDENTE CONFIRMADA CANCELADA CONCLUIDA }

    EntidadeBase <|-- Cliente
    EntidadeBase <|-- Quadra
    EntidadeBase <|-- Equipamento
    EntidadeBase <|-- Reserva
    Cliente <|-- ClienteAvulso
    Cliente <|-- ClienteSocio
    Quadra <|-- QuadraTenis
    Quadra <|-- QuadraBeachTennis

    Descontavel <|.. Cliente
    Precificavel <|.. Quadra
    Precificavel <|.. Equipamento

    Reserva "0..*" o-- "1" Cliente : agregação
    Reserva "0..*" o-- "1" Quadra : agregação
    Reserva "1" *-- "0..*" ItemReserva : composição
    ItemReserva "0..*" --> "1" Equipamento

    ClienteSocio --> PlanoSocio
    QuadraTenis --> TipoPiso
    Reserva --> StatusReserva
```

### Camadas

```mermaid
flowchart LR
    Main["main.Main (CLI)"] --> Services["service.*Service"]
    Services --> Repos["repository.*Repositorio"]
    Repos --> Model["model.*"]
    Services --> Model
    Main -. try-catch .-> Exc["exception.*"]
    Services -. throw .-> Exc
    Main --> Util["util.ConsoleUtil"]
```

| Repositório | Herda de | Interface |
|---|---|---|
| `ClienteRepositorio`, `QuadraRepositorio`, `EquipamentoRepositorio`, `ReservaRepositorio` | `RepositorioMemoria<T>` (abstrata, `LinkedHashMap<Integer,T>`) | `Repositorio<T extends EntidadeBase>` |

| Exceção | Tipo | Uso |
|---|---|---|
| `RegraNegocioException` | unchecked (`RuntimeException`) | base de regras de negócio |
| `DadoInvalidoException` | unchecked, herda da anterior | setters/construtores |
| `HorarioIndisponivelException` | unchecked, herda da anterior | conflito de agenda |
| `EntidadeNaoEncontradaException` | **checked** (`Exception`) | busca por ID/CPF |
| `SaldoInsuficienteException` | **checked** (`Exception`) | débito na carteira |

---

## 2. Estrutura de Diretórios

```
projetopoo/
├── executar.bat
├── README.md
├── GUIA_DEFESA.md
└── src/
    ├── main/
    │   └── Main.java
    ├── model/
    │   ├── EntidadeBase.java          (abstract)
    │   ├── Precificavel.java          (interface)
    │   ├── Descontavel.java           (interface)
    │   ├── Cliente.java               (abstract)
    │   ├── ClienteAvulso.java
    │   ├── ClienteSocio.java
    │   ├── Quadra.java                (abstract)
    │   ├── QuadraTenis.java
    │   ├── QuadraBeachTennis.java
    │   ├── Equipamento.java
    │   ├── Reserva.java
    │   ├── ItemReserva.java
    │   ├── PlanoSocio.java            (enum)
    │   ├── TipoPiso.java              (enum)
    │   └── StatusReserva.java         (enum)
    ├── repository/
    │   ├── Repositorio.java           (interface genérica)
    │   ├── RepositorioMemoria.java    (abstract genérica)
    │   ├── ClienteRepositorio.java
    │   ├── QuadraRepositorio.java
    │   ├── EquipamentoRepositorio.java
    │   └── ReservaRepositorio.java
    ├── service/
    │   ├── ClienteService.java
    │   ├── QuadraService.java
    │   ├── EquipamentoService.java
    │   ├── ReservaService.java
    │   └── RelatorioService.java
    ├── exception/
    │   ├── RegraNegocioException.java
    │   ├── DadoInvalidoException.java
    │   ├── HorarioIndisponivelException.java
    │   ├── EntidadeNaoEncontradaException.java
    │   └── SaldoInsuficienteException.java
    └── util/
        ├── ConsoleUtil.java
        ├── Validador.java
        └── Formatador.java
```

## 3. Regras de Negócio

| Regra | Onde |
|---|---|
| Funcionamento 07:00–22:00, reserva de 1 a 3h | `Reserva.validarHorario` |
| Sem conflito de horário na mesma quadra | `ReservaService.validarDisponibilidade` |
| Não reservar no passado | `ReservaService.validarDataFutura` |
| Limite de reservas ativas: Avulso 2 / Bronze 3 / Prata 4 / Ouro 5 | `getLimiteReservasAtivas()` (polimórfico) |
| Tênis: base × horas × fator piso (+R\$ 25/h coberta) | `QuadraTenis.calcularPreco` |
| Beach: base × horas (+R\$ 15/h coberta, +R\$ 10/h areia premium) | `QuadraBeachTennis.calcularPreco` |
| Acréscimo de 20% nas horas a partir de 18:00 | `Quadra.calcularPreco(int,int)` (final) |
| Desconto: Avulso 0% / Bronze 10% / Prata 20% / Ouro 30% | `aplicarDesconto()` (polimórfico) |
| Pagamento debitado do saldo; sem saldo → exceção | `Cliente.debitar` |
| Cancelamento ≥ 24h: reembolso 100%; < 24h: 50% | `Reserva.calcularValorReembolso` |
| Estoque de equipamentos retirado na reserva e devolvido no cancelamento/conclusão | `ReservaService` |
| CPF único e validado (dígitos verificadores) | `ClienteService.salvarNovo` / `Validador.validarCpf` |

## 4. Dados da carga inicial (`popularDadosIniciais`)

| ID | Cliente | Categoria | Saldo inicial |
|---|---|---|---|
| 1 | Ana Souza | Sócio Ouro | R\$ 500 |
| 2 | Bruno Lima | Sócio Prata | R\$ 300 |
| 3 | Carla Mendes | Avulso | R\$ 200 |
| 4 | Diego Rocha | Avulso | R\$ 30 (demonstra `SaldoInsuficienteException`) |

Quadras: 1 Central Saibro, 2 Rápida Coberta, 3 Arena Beach 1, 4 Arena Beach Premium.
Equipamentos: 1 Raquete de Tênis, 2 Raquete de Beach Tennis, 3 Tubo de Bolinhas.
Reservas: 3 já confirmadas para amanhã/depois de amanhã (relativas à data de execução).
