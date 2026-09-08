# Mini-autorizador — execução e decisões

API REST para criação de cartões, consulta de saldo e autorização de transações. O enunciado e os contratos originais estão no [README.md](README.md).

## Tecnologias

Java 17, Spring Boot 4.1.1, Maven, Spring Data JPA, MySQL 5.7, Liquibase e Bean Validation. Lombok reduz o código repetitivo de construtores e getters.

## Executar localmente

Pré-requisitos: JDK 17, Docker com Compose e portas 3306 e 8080 disponíveis. Execute os comandos na raiz do projeto. O Maven Wrapper baixa o Maven na primeira execução; o download das dependências também requer acesso à internet.

Inicie somente o banco escolhido no Compose fornecido:

```bash
docker compose -f docker/docker-compose.yml up -d mysql
```

Em máquinas Apple Silicon, utilize emulação para a imagem MySQL 5.7:

```bash
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose -f docker/docker-compose.yml up -d mysql
```

Aguarde o MySQL estar pronto para conexões e inicie a aplicação:

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O Liquibase cria a estrutura na primeira inicialização e verifica o histórico nas seguintes. O Hibernate valida o mapeamento das entidades, sem criar ou atualizar tabelas.

Para gerar e executar o JAR:

```bash
./mvnw clean package
java -jar target/mini-autorizador-0.0.1-SNAPSHOT.jar
```

Execute a aplicação por uma das duas opções. No Windows, substitua `./mvnw` por `mvnw.cmd`. Também é possível usar `mvn` quando o Maven estiver instalado.

### Configuração do banco

| Variável | Valor padrão |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `miniautorizador` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | vazio |

Os padrões correspondem ao serviço MySQL do Compose fornecido, cuja declaração foi preservada. O módulo `hibernate-community-dialects` fornece o `MySQLLegacyDialect` utilizado para o MySQL 5.7.

## Testes automatizados

```bash
./mvnw clean test
```

A suíte utiliza JUnit, AssertJ, Mockito e MockMvc standalone. Não inicia a aplicação, não sobe containers e não conecta a bancos ou serviços externos. O Docker não precisa estar em execução para rodar os testes.

Os testes verificam domínio, serviços, conversões utilizadas, tratamento de erros, validações e contratos HTTP. Controllers usam serviços mockados; serviços e adapters usam repositórios mockados. A execução do SQL, as transações reais e a persistência entre reinícios exigem validação separada com a aplicação conectada ao MySQL.

## Exemplo de uso

Com a aplicação iniciada, crie um cartão cujo número ainda não esteja cadastrado:

```bash
curl -i -X POST http://localhost:8080/cartoes \
  -H 'Content-Type: application/json' \
  -d '{"numeroCartao":"6549873025634501","senha":"1234"}'
```

Resposta: `201` com número e senha enviados. O saldo inicial é `500.00`.

```bash
curl -i http://localhost:8080/cartoes/6549873025634501
```

Resposta: `200` com o saldo numérico, sem objeto envolvendo o valor.

```bash
curl -i -X POST http://localhost:8080/transacoes \
  -H 'Content-Type: application/json' \
  -d '{"numeroCartao":"6549873025634501","senhaCartao":"1234","valor":10.00}'
```

Resposta: `201` com texto `OK`. Após essa compra, a consulta retorna `490.00`.

| Situação | Resposta |
| --- | --- |
| Criação de cartão duplicado | 422, JSON com número e senha da tentativa, sem alterar o cartão existente |
| Consulta de cartão inexistente | 404, sem corpo |
| Transação com cartão inexistente | 422, texto `CARTAO_INEXISTENTE` |
| Transação com senha incorreta | 422, texto `SENHA_INVALIDA` |
| Transação sem saldo suficiente | 422, texto `SALDO_INSUFICIENTE` |
| Entrada inválida ou JSON malformado | 400 |

## Organização e decisões

- `controller`: contratos HTTP; 
- `api`: tratamento centralizado das exceções de negócio.
- `service`: casos de uso e limites transacionais.
- `model`: domínio sem anotações JPA, requests e responses.
- `repository`: interface de persistência, adapter, repositório Spring Data e entidade JPA.
- `mapper`: conversão entre representações; `exceptions` e `util/enums`: erros e motivos de recusa.

O número do cartão é uma string e a chave primária, preservando zeros à esquerda. A criação utiliza `INSERT`, sem atualizar registros existentes. A chave primária garante unicidade; somente o erro MySQL de chave duplicada, código 1062 e SQLState 23000, é convertido em duplicidade de cartão.

Valores monetários usam `BigDecimal` e coluna `DECIMAL(19,2)`. Cartões iniciam com `500.00`. As verificações de autorização seguem a ordem cartão existente, senha correta e saldo suficiente. Uma compra pode consumir todo o saldo; uma recusa não executa o débito. Transações não são armazenadas, conforme permitido pelo enunciado.

### Suposições sobre entradas

- Número do cartão obrigatório, com até 32 caracteres; sem imposição de tamanho fixo ou algoritmo de validação.
- Senha obrigatória, com até 100 caracteres, preservada sem remover espaços ou zeros à esquerda. Valores compostos somente por espaços são inválidos.
- Valor de transação obrigatório, mínimo `0.01`, com até 17 dígitos inteiros e duas casas decimais. Valores com mais casas são rejeitados, sem arredondamento, inclusive `10.000`.
- Entradas que violam essas validações retornam 400. O formato de quatro dígitos não é inferido do exemplo do enunciado nesta versão.

### Escopo desta versão

O desafio sem `if` está implementado no código da aplicação, sem utilizar `break` ou `continue`. As verificações usam `Optional`, `filter`, `map` e `orElseThrow`, preservando a ordem das regras e os motivos de recusa.

O débito calcula o novo saldo a partir da leitura e o grava dentro de uma transação; isso ainda não garante correção entre compras simultâneas. A proteção de concorrência permanece para uma evolução separada.

As senhas ainda são armazenadas sem hash. BCrypt e a restrição de quatro dígitos serão incorporados em evoluções separadas, mantendo os contratos da API.
