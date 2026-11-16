# Guia de Defesa — Arena Smash (POO)

## 1. Mapa dos pilares no código

### Encapsulamento
| O quê | Onde |
|---|---|
| 100% dos atributos `private` | todas as classes de `model` |
| Setters com validação (nunca aceitam lixo) | `Cliente.setNome/setEmail/setTelefone`, `Quadra.setValorHoraBase`, `Equipamento.setValorHora`, `ClienteSocio.setPlano` → delegam a `util.Validador` e lançam `DadoInvalidoException` |
| Atributo sem setter (muda só via regra) | `Cliente.saldo` → só `depositar()` / `debitar()` |
| Atributos imutáveis (`final`) | `Cliente.cpf`, `Reserva.cliente/quadra/data/horaInicio` |
| ID atribuído uma única vez | `EntidadeBase.definirId()` |
| Lista protegida contra escrita externa | `Reserva.getItens()` → `Collections.unmodifiableList` |
| Cópia defensiva | `RepositorioMemoria.listarTodos()` |
| Máquina de estados protegida | `Reserva.confirmar/cancelar/concluir` + `exigirStatus()` |

### Abstração e Herança
| Superclasse abstrata | Subclasses concretas |
|---|---|
| `EntidadeBase` | `Cliente`, `Quadra`, `Equipamento`, `Reserva` |
| `Cliente` | `ClienteAvulso`, `ClienteSocio` |
| `Quadra` | `QuadraTenis`, `QuadraBeachTennis` |
| `RepositorioMemoria<T>` | 4 repositórios concretos |
| `RegraNegocioException` | `DadoInvalidoException`, `HorarioIndisponivelException` |

Uso de `super(...)` nos construtores: `ClienteSocio`, `QuadraTenis`, `QuadraBeachTennis`. Uso de `super.toString()`: `ClienteSocio.toString()`.

### Polimorfismo
| Tipo | Onde |
|---|---|
| Sobrescrita de método abstrato | `calcularPreco(int)` em `QuadraTenis` / `QuadraBeachTennis`; `aplicarDesconto()` e `getLimiteReservasAtivas()` em `ClienteAvulso` / `ClienteSocio`; `getDescricaoResumida()` em todas as entidades |
| `toString()` | `Cliente`, `ClienteSocio`, `Quadra`, `Equipamento`, `Reserva`, `ItemReserva`, enums |
| `equals()` / `hashCode()` | `EntidadeBase` (por ID), `Cliente` (por CPF — chave natural), `ItemReserva` (por equipamento) |
| Sobrecarga de construtores | `Cliente`, `ClienteSocio`, `ClienteAvulso`, `Quadra`, `QuadraTenis`, `QuadraBeachTennis`, `Equipamento`, `Reserva` (1h padrão), `EntidadeNaoEncontradaException` |
| Sobrecarga de métodos | `Quadra.calcularPreco(int)` × `calcularPreco(int,int)`; `Equipamento.calcularPreco(int)` × `(int,int)`; `ReservaService.criarReserva` (com/sem equipamentos); `ConsoleUtil.lerInteiro`; `Validador.validarTextoObrigatorio` |
| Coleção da superclasse com despacho dinâmico | `RelatorioService.listarTodasEntidades()` → `List<EntidadeBase>` + `Main.relatorioVisaoGeral()` |
| Coleção da interface | `RelatorioService.listarItensPrecificaveis()` → `List<Precificavel>` + `Main.relatorioTabelaPrecos()` |
| Polimorfismo dentro da regra de negócio | `Reserva.calcularValorFinal()` chama `cliente.aplicarDesconto()` sem saber o tipo; `ReservaService.validarLimiteReservas()` usa `getLimiteReservasAtivas()` |
| Template Method | `Quadra.calcularPreco(int,int)` é `final` e chama o abstrato `calcularPreco(int)` |

### Interfaces
| Interface | Implementada por | Detalhe |
|---|---|---|
| `Precificavel` | `Quadra` **e** `Equipamento` (sem relação de herança entre si) | contrato comum de preço |
| `Descontavel` | `Cliente` (concretizado nas subclasses) | possui método `default descreverDesconto()` |
| `Repositorio<T>` | `RepositorioMemoria<T>` | genérica, permite trocar a persistência |
| `AcaoMenu` (interna, `@FunctionalInterface`) | method references em `Main` | centraliza o try-catch |

### Exceções personalizadas
| Exceção | Lançada em | Tratada em |
|---|---|---|
| `EntidadeNaoEncontradaException` (checked) | `*Service.buscarPorId` | `Main.executarComTratamento` |
| `SaldoInsuficienteException` (checked) | `Cliente.debitar` | idem (mostra valor faltante) |
| `HorarioIndisponivelException` | `ReservaService.validarDisponibilidade` | idem |
| `DadoInvalidoException` | `Validador`, setters, construtores | idem (via `RegraNegocioException`) |
| `RegraNegocioException` | services e `Reserva` | idem |

`catch` ordenado do mais específico ao mais genérico; `catch (Exception e)` final garante que o programa nunca quebra. Input inválido (letras em campo numérico, data errada) é tratado em `ConsoleUtil` com loop + `NumberFormatException` / `DateTimeParseException`.

### Composição × Agregação
| Relação | Tipo | Justificativa |
|---|---|---|
| `Reserva` ◆— `ItemReserva` | **Composição** | construtor de `ItemReserva` é package-private; só `Reserva.adicionarItem()` cria; item não existe sem a reserva |
| `Reserva` ◇— `Cliente` / `Quadra` | **Agregação** | existem antes e depois da reserva |
| `ItemReserva` → `Equipamento` | Associação/Agregação | item só referencia o equipamento do catálogo |

### Bug do Scanner
`ConsoleUtil` usa **apenas `nextLine()`** e converte com `Integer.parseInt` / `Double.parseDouble`. Nunca há `nextInt()`, logo não sobra `\n` no buffer.

---

## 2. Roteiro de demonstração (≈ 5 min)

1. **Relatórios → 1**: `List<EntidadeBase>` com 4 classes diferentes respondendo `getDescricaoResumida()`.
2. **Relatórios → 2**: `List<Precificavel>`: quadras e equipamentos com preços calculados por regras diferentes.
3. **Relatórios → 3** (valor 100): mesmo método `aplicarDesconto`, resultados 70 / 80 / 100.
4. **Reservas → 1** com Diego (ID 4), quadra 1, amanhã, 14h, 1h → orçamento R\$ 80 > saldo R\$ 30 → `SaldoInsuficienteException`.
5. **Reservas → 1** com Carla (ID 3), quadra 1, amanhã, 10h → `HorarioIndisponivelException` (Ana já reservou 09–11h).
6. **Clientes → 1** com CPF `111.111.111-11` → `DadoInvalidoException` (encapsulamento).
7. **Reservas → 5** com ID 99 → `EntidadeNaoEncontradaException`.
8. Digitar `abc` em qualquer menu → programa segue funcionando.
9. **Reservas → 6** (cancelar reserva 1) → reembolso 50% (< 24h) e estoque de raquetes devolvido (**Equipamentos → 2**).

---

## 3. Perguntas prováveis do professor

**Por que `Cliente` é abstrata e não uma classe concreta com um campo `tipo`?**
Evita `if/switch` por tipo espalhados (violação do Open/Closed). Novo tipo de cliente = nova subclasse, sem alterar `ReservaService`.

**Por que `Precificavel` é interface e não superclasse?**
`Quadra` e `Equipamento` não têm relação "é um" entre si; compartilham apenas a capacidade de ter preço. Java não tem herança múltipla de classes, mas uma classe pode implementar várias interfaces.

**Diferença entre classe abstrata e interface?**
Abstrata: tem estado (atributos), construtor, métodos concretos; herança única. Interface: contrato sem estado de instância (só constantes), pode ter métodos `default`/`static`; implementação múltipla.

**Por que algumas exceções são checked e outras unchecked?**
Checked (`EntidadeNaoEncontrada`, `SaldoInsuficiente`) representam situações esperadas que o chamador **deve** tratar — o compilador obriga. Unchecked (`RegraNegocio` e filhas) são violações de pré-condição que podem surgir em qualquer camada; forçar `throws` em todo método poluiria as assinaturas.

**Por que `equals` de `Cliente` usa CPF e não o ID?**
CPF é a chave natural do negócio: dois objetos com o mesmo CPF representam a mesma pessoa, mesmo antes de ganharem ID. `hashCode` foi sobrescrito junto para manter o contrato (`equals` iguais ⇒ `hashCode` iguais), essencial em `HashMap`/`HashSet`.

**O que acontece se sobrescrever `equals` sem `hashCode`?**
Objetos "iguais" cairiam em buckets diferentes de um `HashSet`/`HashMap`, gerando duplicatas e buscas falhando.

**Onde está a composição? Qual a diferença para agregação?**
Composição: `Reserva`–`ItemReserva` (ciclo de vida dependente; construtor package-private). Agregação: `Reserva`–`Cliente`/`Quadra` (objetos independentes).

**Por que a camada `service`? Não bastava colocar tudo no `Main`?**
Separação de responsabilidades: `Main` só faz I/O; regras ficam testáveis e reutilizáveis (ex: trocar CLI por API REST sem tocar em regra).

**Por que `Repositorio` é interface genérica?**
Inversão de dependência: amanhã pode existir `RepositorioCsv<T>` ou JDBC sem alterar services. Generics (`T extends EntidadeBase`) evitam casts e duplicação de código.

**Por que `Quadra.calcularPreco(int, int)` é `final`?**
Padrão Template Method: a regra noturna é igual para todas as quadras e não pode ser alterada; a parte variável (`calcularPreco(int)`) fica abstrata.

**Por que `getItens()` retorna lista não modificável?**
Se retornasse a lista real, qualquer código poderia adicionar itens sem passar pela validação de `adicionarItem()` (quebra de encapsulamento).

**Por que `double` para dinheiro?**
Simplicidade didática, com arredondamento a 2 casas em `Formatador.arredondarMoeda`. Em produção, o correto é `BigDecimal` (precisão exata em base decimal).

**Como o sistema evita reserva em conflito?**
`Reserva.conflitaCom()` aplica a verificação de sobreposição de intervalos `inicioA < fimB && fimA > inicioB`, ignorando reservas canceladas.

**Por que `Reserva.cancelar` recebe `LocalDateTime` em vez de chamar `now()` internamente?**
Testabilidade: o modelo fica determinístico; o service injeta o momento atual.
