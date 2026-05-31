# Coworking API

API REST para gestão de reservas de salas e auditórios de um espaço de coworking.
Permite cadastrar salas, reservar horários, evitar conflitos, cancelar reservas,
consultar a agenda de um dia e descobrir quais salas estão livres em uma data.

## Funcionalidades

- CRUD de salas: cadastro, listagem (paginada, com filtro por tipo), atualização e exclusão
- Nome de sala único (rejeita duplicados)
- Disponibilidade de salas por data, com os horários livres do dia; opcionalmente filtrada por um intervalo de horário
- Criação de reservas para um dia e horário
- Validação de conflito de horário (impede duas reservas sobrepostas na mesma sala)
- Validação de data passada e de horário dentro do funcionamento (08:00–22:00)
- Cancelamento de reservas
- Agenda diária (todas as reservas de um dia, ordenadas por horário)

## Stack

- Java 17
- Spring Boot 4.0.6 (Web MVC, Spring Data JPA, Bean Validation)
- Banco H2 em memória
- springdoc-openapi (Swagger UI)
- Testes: JUnit 5, Mockito e AssertJ
- Cobertura de testes: JaCoCo (mínimo de 85%, o build falha abaixo disso)

## Como executar

Pré-requisito: JDK 17 instalado.

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run` (ou `.\mvnw spring-boot:run` no PowerShell).

A aplicação sobe em `http://localhost:8080`.

| Recurso | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console H2 | http://localhost:8080/h2-console |

Conexão do console H2: **JDBC URL** `jdbc:h2:mem:coworking`, **usuário** `sa`, **senha** em branco.

## Endpoints

### Salas

| Método | Rota | Descrição | Sucesso |
| --- | --- | --- | --- |
| POST | `/salas` | Cadastra uma sala | 201 |
| PUT | `/salas/{id}` | Atualiza uma sala | 200 |
| DELETE | `/salas/{id}` | Exclui uma sala (bloqueada se houver reservas futuras) | 204 |
| GET | `/salas` | Lista as salas, **paginada** | 200 |
| GET | `/salas?tipo=INDIVIDUAL` | Lista as salas de um tipo, paginada | 200 |
| GET | `/salas/disponibilidade?data=AAAA-MM-DD` | Salas com os horários livres do dia | 200 |
| GET | `/salas/disponibilidade?data=AAAA-MM-DD&horaInicio=HH:MM&horaFim=HH:MM` | Salas livres no intervalo informado | 200 |

> A listagem `GET /salas` é paginada e aceita os parâmetros padrão do Spring Data: `page`, `size` e `sort` (ex.: `?page=0&size=20&sort=nome,asc`).

### Reservas

| Método | Rota | Descrição | Sucesso |
| --- | --- | --- | --- |
| POST | `/reservas` | Cria uma reserva | 201 |
| DELETE | `/reservas/{id}` | Cancela uma reserva | 204 |
| GET | `/reservas?data=AAAA-MM-DD` | Agenda do dia (ordenada por horário) | 200 |

## Exemplos

> O banco H2 é em memória e **inicia vazio** a cada execução. Rode os exemplos na ordem: cadastre uma sala antes de criar uma reserva (a primeira sala cadastrada recebe `id = 1`).

Cadastrar uma sala:

```bash
curl -X POST http://localhost:8080/salas \
  -H "Content-Type: application/json" \
  -d '{"nome":"Sala Azul","tipo":"COLETIVA","capacidade":10}'
```

Criar uma reserva:

```bash
curl -X POST http://localhost:8080/reservas \
  -H "Content-Type: application/json" \
  -d '{"salaId":1,"data":"2026-06-01","horaInicio":"10:00","horaFim":"11:00","responsavel":"Caio","email":"caio@email.com"}'
```

Agenda de um dia:

```bash
curl "http://localhost:8080/reservas?data=2026-06-01"
```

Salas com os horários livres em um dia:

```bash
curl "http://localhost:8080/salas/disponibilidade?data=2026-06-01"
```

Salas livres em um intervalo específico:

```bash
curl "http://localhost:8080/salas/disponibilidade?data=2026-06-01&horaInicio=09:00&horaFim=10:00"
```

Listar salas de um tipo, paginado:

```bash
curl "http://localhost:8080/salas?tipo=INDIVIDUAL&page=0&size=20&sort=nome,asc"
```

Atualizar uma sala:

```bash
curl -X PUT http://localhost:8080/salas/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Sala Verde","tipo":"COLETIVA","capacidade":12}'
```

Excluir uma sala:

```bash
curl -X DELETE http://localhost:8080/salas/1
```

Cancelar uma reserva:

```bash
curl -X DELETE http://localhost:8080/reservas/1
```

## Regras de negócio e premissas

- **Tipos de sala:** `COLETIVA`, `INDIVIDUAL`, `AUDITORIO`.
- **Capacidade:** maior que 2 e menor que 50 (de 3 a 49).
- **Nome único:** não é permitido cadastrar/atualizar duas salas com o mesmo nome (`409 Conflict`).
  A restrição é garantida também por *constraint* de unicidade no banco.
- **Funcionamento:** o coworking opera das **08:00 às 22:00**; reservas fora dessa janela são rejeitadas (`400`).
- **Data passada:** não é permitido reservar em uma data anterior a hoje (`400`).
- **Conflito de horário:** duas reservas conflitam quando são na mesma sala, no mesmo dia,
  e os intervalos se sobrepõem. Horários que apenas se encostam (o fim de uma é o início da
  outra) **não** conflitam. Uma tentativa de reserva conflitante retorna `409 Conflict`.
- **Disponibilidade:** os horários livres de uma sala são a janela de funcionamento (08:00–22:00)
  menos as reservas do dia. Sem `horaInicio`/`horaFim`, retorna as salas que têm algum horário livre,
  com a lista de intervalos livres. Com um intervalo informado, retorna apenas as salas livres naquele
  intervalo — e, para cada uma, a lista `horariosLivres` continua mostrando todos os intervalos livres do dia.
- **Exclusão de sala:** bloqueada (`409 Conflict`) se existirem reservas **futuras** vinculadas à sala.
- **Cancelamento:** remove a reserva definitivamente.
- **Erros** retornam JSON padronizado com `timestamp`, `status`, `erro`, `mensagem` e,
  nas validações, um mapa `campos` com a mensagem de cada campo inválido.

## Testes

```bash
./mvnw verify
```

Executa a suíte completa e a verificação de cobertura (JaCoCo). O build falha se a
cobertura de linhas ficar abaixo de 85%.

## Estrutura do projeto

```
src/main/java/com/coworking
├── config        Configuração do OpenAPI/Swagger
├── controller    Endpoints REST (Salas e Reservas)
├── domain        Regras de horário: sobreposição/disponibilidade (IntervaloHorario) e janela de funcionamento (Funcionamento)
├── dto           Objetos de entrada (request) e saída (response)
├── entity        Entidades JPA (Sala, Reserva)
├── enums         TipoSala
├── exception     Exceções de domínio e handler global de erros
├── mapper        Conversão entre entidades e DTOs
├── repository    Repositórios Spring Data JPA
└── service       Regras de negócio (interface + implementação)
```
