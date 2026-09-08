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

### Cobertura com JaCoCo

```bash
./mvnw clean verify
```

O comando executa a suíte, empacota a aplicação e gera os relatórios de cobertura em `target/site/jacoco/`: `index.html` para navegação no navegador, `jacoco.xml` e `jacoco.csv` para ferramentas externas. O agente é preparado antes dos testes; o relatório é gerado na fase `verify`. Executar somente `test` não gera o relatório.

Não há limite mínimo de cobertura nem exclusões personalizadas. A configuração mede a cobertura, sem reprovar o build por percentual. O diretório `target/` já está ignorado pelo Git.

Na execução desta configuração, os 74 testes passaram: cobertura de linhas de 94,87% (74/78) e de branches de 100% (8/8). As quatro linhas não cobertas pertencem à inicialização da aplicação e à conversão `CartaoMapper.toEntity`, que não é usada pela inserção SQL atual. Código gerado pode ser filtrado automaticamente pelo JaCoCo.

Esses percentuais representam a execução da suíte isolada; não comprovam o funcionamento do SQL ou da concorrência no banco. As validações reais permanecem separadas.

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

Erros de validação dos campos retornam HTTP 400 com o campo e a mensagem, sem incluir o valor rejeitado. Exemplo de senha com cinco dígitos:

```json
{
  "status": 400,
  "erros": [
    {
      "campo": "senha",
      "mensagem": "A senha deve conter exatamente 4 dígitos numéricos."
    }
  ]
}
```

A lista pode conter mais de um erro. JSON malformado continua retornando 400 pelo tratamento padrão do Spring. Os contratos de duplicidade, cartão inexistente e recusa de transação permanecem conforme a tabela acima.

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
- Na criação, a senha deve conter exatamente quatro dígitos ASCII (`0` a `9`). Zeros à esquerda são preservados; letras, espaços, símbolos e outros tamanhos retornam 400.
- Nas transações, `senhaCartao` permanece obrigatória e limitada a 100 caracteres; uma senha que não corresponde ao hash armazenado resulta em `SENHA_INVALIDA`.
- Valor de transação obrigatório, mínimo `0.01`, com até 17 dígitos inteiros e duas casas decimais. Valores com mais casas são rejeitados, sem arredondamento, inclusive `10.000`.
- Entradas que violam essas validações retornam 400. A restrição de quatro dígitos na criação é uma decisão adicional ao enunciado.

### Escopo desta versão

O desafio sem `if` está implementado no código da aplicação, sem utilizar `break` ou `continue`. As verificações usam `Optional`, `filter`, `map` e `orElseThrow`, preservando a ordem das regras e os motivos de recusa.

A autorização verifica a senha e executa um débito atômico no banco, dentro da transação. O saldo consultado anteriormente não é utilizado para calcular o novo saldo. A atualização segue esta condição:

```sql
UPDATE cartao
   SET saldo = saldo - :valor
 WHERE numero_cartao = :numeroCartao
   AND saldo >= :valor
```

Uma linha atualizada representa aprovação. Nenhuma linha atualizada, após a verificação de existência e senha, resulta em `SALDO_INSUFICIENTE`. O InnoDB coordena as atualizações concorrentes sobre o mesmo cartão, inclusive entre instâncias diferentes da aplicação, sem bloqueios em memória da JVM.

Com saldo de 10.00, duas compras simultâneas de 10.00 resultam em uma aprovação e uma recusa, com saldo final zero. Com saldo de 20.00, ambas podem ser aprovadas. Esta operação não implementa idempotência: cada requisição válida representa uma nova compra.

A validação separada com MySQL 5.7 e duas JVMs confirmou 20 rodadas do primeiro cenário e duas do segundo. Os saldos foram consultados pelas duas instâncias após cada rodada e confirmados por SQL ao final. Também foram executadas 120 rodadas com quatro JVMs reais: grupos de três ou quatro compras simultâneas sobre o mesmo cartão, com saldo para uma, duas ou todas as compras. As 420 requisições concorrentes produziram exatamente as aprovações permitidas pelo saldo, e os 120 cartões terminaram com saldo zero, conferido nas quatro instâncias e por SQL. Essas validações não integram a suíte unitária e não equivalem a um teste de carga.

### Armazenamento de senhas

Novos cartões armazenam somente o hash BCrypt, com custo 12 e salt aleatório. A coluna existente comporta o hash de 60 caracteres, sem alteração de schema. A conferência de senha usa BCrypt; o hash nunca é devolvido pela API. As respostas de criação e duplicidade contêm a senha enviada na requisição, conforme o contrato, inclusive quando a duplicidade é detectada pela chave primária.

Foi adicionado apenas o módulo `spring-security-crypto`; os endpoints continuam sem autenticação HTTP adicional.

Cartões antigos com senha em texto puro não são migrados nem excluídos automaticamente e deixam de autenticar. Para testar esta versão, crie cartões com números novos ou utilize um banco separado por meio de `DB_NAME`. Hashes BCrypt já existentes continuam sendo verificados normalmente.
