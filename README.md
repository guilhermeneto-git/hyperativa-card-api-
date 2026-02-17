# Card API

API REST para gerenciamento de cartões com autenticação JWT.

## Tecnologias

- Java 17
- Spring Boot 3.2.2
- MySQL
- JWT Authentication
- Swagger/OpenAPI

## Requisitos

- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Configuração do Banco de Dados

A aplicação utiliza **MySQL**. Configure as credenciais em `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/card_db
spring.datasource.username=root
spring.datasource.password=root
```

**Importante:** Ao inicializar a aplicação, toda a estrutura do banco de dados será criada automaticamente (database, tabelas e usuários padrão).

## Usuários Padrão

Após a primeira execução, os seguintes usuários estarão disponíveis:

| Username | Password | Role |
|----------|----------|------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER |

## Executar a Aplicação

```bash
mvn clean install
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080/api`

## Documentação da API (Swagger)

Acesse a documentação interativa da API:

**http://localhost:8080/api/swagger-ui/index.html**

O Swagger é autointuitivo e permite testar todos os endpoints diretamente pela interface.

### Fluxo de Autenticação no Swagger:

1. Acesse o endpoint `/auth/login`
2. Use as credenciais (admin/admin123)
3. Copie o token JWT retornado
4. Clique no botão "Authorize" (cadeado verde)
5. Cole o token no formato: `Bearer <seu-token>`
6. Teste os demais endpoints autenticados

---

**Repositório base:** https://github.com/hyperativa/back-end

