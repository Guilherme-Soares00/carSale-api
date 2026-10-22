# Car Sale API

API REST do exercício Car Sale, implementada com Java 25, Spring Boot 3.5.16 e Spring MVC. Permite consultar carros e comentários, publicar comentários com autenticação e excluir comentários com perfil de administrador.

## Integrantes

| Integrante | RM |
| --- | --- |
| Guilherme Soares | RM568227 |
| Gabriel Stuani | RM566682 |

## Tecnologias

Java 25, Spring Boot 3.5.16, Spring MVC, Spring Data JPA, Spring Security, OAuth2 Resource Server, JWT RS256, BCrypt, Bean Validation, H2, JUnit e Maven Wrapper 3.9.11. Não depende de Lombok nem de plugins de geração de código no IntelliJ.

## Abrir no IntelliJ IDEA

1. Extraia o ZIP antes de abrir o projeto.
2. No IntelliJ, escolha **File > Open** e selecione esta pasta ou seu `pom.xml`. Importe como projeto Maven.
3. Em **File > Project Structure > Project**, selecione um **JDK 25** em **Project SDK**. Use **Download JDK** se necessário. A versão do IntelliJ deve oferecer suporte ao JDK 25.
4. Em **Settings > Build, Execution, Deployment > Build Tools > Maven**, use o Maven Wrapper. Em **Runner > JRE** e **Importing > JDK for importer**, selecione o mesmo JDK 25.
5. Recarregue o projeto Maven e aguarde as dependências serem baixadas.
6. Execute `CarsaleApiApplication` pelo ícone ao lado de `main`, ou use a configuração compartilhada **CarSale API**.
7. A API ficará disponível em `http://localhost:8080/cars`.

A primeira execução requer internet para baixar dependências. O Maven verifica a versão do Java e apresenta uma mensagem clara se o JDK não for o 25.

## Executar no terminal

PowerShell, dentro de `carsale-api`:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw clean verify
./mvnw spring-boot:run
```

Também é possível executar o JAR depois do build:

```bash
java -jar target/carsale-api-1.0.0.jar
```

## Contas de desenvolvimento

O perfil padrão é `dev`. Ele cria seis carros e duas contas locais:

| Usuário | Perfil |
| --- | --- |
| `usuario` | `ROLE_USER` |
| `admin` | `ROLE_ADMIN` |

As senhas são geradas aleatoriamente a cada inicialização e aparecem uma vez no console com o prefixo `CARSALE DEV`. Não há senha fixa no código. Use as senhas exibidas para entrar no frontend.

Para escolher suas próprias senhas, adicione `CARSALE_USER_PASSWORD` e `CARSALE_ADMIN_PASSWORD` às variáveis de ambiente da configuração de execução do IntelliJ. Cada senha deve ter pelo menos 12 caracteres e no máximo 72 bytes em UTF-8. Reinicie a aplicação depois de configurar as variáveis.

O H2 fica em memória: dados e comentários são reiniciados ao encerrar a aplicação. As chaves RSA locais também são temporárias, portanto um reinício invalida tokens anteriores. Faça login novamente após reiniciar o backend.

## Endpoints e permissões

| Método | Endpoint | Visitante | Usuário | Administrador |
| --- | --- | --- | --- | --- |
| GET | `/cars` | Sim | Sim | Sim |
| GET | `/cars/{id}` | Sim | Sim | Sim |
| GET | `/comments` | Sim | Sim | Sim |
| GET | `/comments/{id}` | Sim | Sim | Sim |
| POST | `/login` | Sim | Sim | Sim |
| GET | `/me` | Não | Sim | Sim |
| POST | `/comments` | Não | Sim | Sim |
| DELETE | `/comments/{id}` | Não | Não | Sim |

Operações não previstas são negadas. Use `Authorization: Bearer SEU_TOKEN` nas requisições autenticadas. Os tokens expiram em 30 minutos.

Login:

```json
{
  "username": "usuario",
  "password": "senha exibida no console"
}
```

A resposta contém `token`, `name`, `id`, `role` e `expiresIn`.

Criação de comentário:

```json
{
  "comment": "Gostei do conforto deste carro.",
  "carId": 1
}
```

O autor é obtido exclusivamente do JWT validado. O campo legado `userId`, caso enviado, é ignorado. O comentário deve ter conteúdo e no máximo 2000 caracteres. A criação retorna `201 Created`, DTO do comentário e `Location`; a exclusão retorna `204 No Content`.

Erros possuem `status` e `message`: `400` para entrada inválida, `401` para falha de autenticação, `403` para falta de permissão e `404` para recurso inexistente.

## Arquitetura

- `auth`: autenticação, BCrypt, emissão e validação de JWT e regras de acesso.
- `domain/car`: entidades, repositório, serviço e controller do catálogo.
- `domain/comments`: comentários, validação, DTOs e autorização por método.
- `domain/user`: usuários e representação pública sem senha.
- `config`: carga de usuários somente no perfil de desenvolvimento.
- `exception`: respostas de erro centralizadas.
- `props`: configuração das chaves RSA externas.

Controllers recebem DTOs e delegam regras aos serviços. Os serviços delimitam transações. As consultas carregam os relacionamentos necessários explicitamente para evitar N+1. `open-in-view` está desativado e nenhuma entidade de usuário é retornada pela API.

As listagens preservam o contrato de arrays do frontend fornecido pelo exercício. Para grandes catálogos, uma evolução deve paginar carros e comentários em conjunto com a adaptação do frontend.

## Integração com o frontend

Este repositório contém somente o backend. Ele expõe a API na porta 8080 e pode ser consumido pelo [frontend do exercício](https://github.com/joaocarloslima/carsale), executado separadamente na porta 3000. As chamadas autenticadas à API usam o header Bearer. A API não autentica por cookies.

As URLs dos projetos-base do enunciado são [carsale-api](https://github.com/joaocarloslima/carsale-api) e [carsale](https://github.com/joaocarloslima/carsale). Os contratos públicos foram preservados e a integração foi ajustada para obter a autoria do usuário autenticado.

## Testes

```powershell
.\mvnw.cmd clean verify
```

Os testes exercitam o banco H2, o encoder BCrypt, emissão e validação reais de JWT e a cadeia completa de filtros com MockMvc. Não usam mocks. Cobrem visitantes, usuários, administradores, autoria forjada, erros de entrada, tokens expirados ou adulterados, emissor incorreto e quantidade de consultas do catálogo.

## Configurações e produção

| Variável | Uso |
| --- | --- |
| `PORT` | Porta da API, padrão 8080 |
| `CARSALE_ALLOWED_ORIGINS` | Origens CORS separadas por vírgula, padrão `http://localhost:3000` |
| `CARSALE_USER_PASSWORD` | Senha escolhida para a conta local `usuario` |
| `CARSALE_ADMIN_PASSWORD` | Senha escolhida para a conta local `admin` |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo; `prod` exige configuração externa |
| `RSA_PUBLIC_KEY` | Localização da chave pública PEM X.509, por exemplo `file:C:/segredos/public.pem` |
| `RSA_PRIVATE_KEY` | Localização da chave privada PEM PKCS#8, por exemplo `file:C:/segredos/private.pem` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexão externa no perfil `prod` |

O perfil `prod` desativa carga demonstrativa e criação automática de tabelas, exige um par RSA correspondente de pelo menos 2048 bits e valida o esquema do banco. O driver incluído é H2; outro banco exige seu driver JDBC e provisionamento do esquema. Esta entrega é voltada ao exercício local. Para publicação, configure HTTPS, provisionamento de usuários, migrations e limitação de tentativas de login conforme o ambiente.
