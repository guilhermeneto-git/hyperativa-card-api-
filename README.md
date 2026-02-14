# Card API - Hyperativa Challenge

API REST para gerenciamento de cartões com autenticação JWT. Permite cadastro individual de cartões, upload de arquivo em lote e consulta por número.

---

## 📋 Índice

- [Tecnologias](#tecnologias)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Requisitos](#requisitos)
- [Configuração e Execução](#configuração-e-execução)
- [Autenticação JWT](#autenticação-jwt)
- [Endpoints da API](#endpoints-da-api)
- [Testando com Swagger](#testando-com-swagger)
- [Upload de Arquivo](#upload-de-arquivo)
- [Exemplos com cURL](#exemplos-com-curl)

---

## 🚀 Tecnologias

- **Java 17** (obrigatório - incompatível com Java 21 devido ao Lombok)
- **Spring Boot 3.2.2**
- **Spring Security** - Autenticação JWT
- **Spring Data JPA** - Persistência
- **H2 Database** - Banco em memória
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI 3** - Documentação Swagger
- **Maven** - Gerenciamento de dependências

---

## 📁 Estrutura do Projeto

```
card/
├── src/main/java/com/hyperativa/card/
│   ├── config/
│   │   ├── DataInitializer.java         # Cria usuários padrão na inicialização
│   │   └── OpenApiConfig.java           # Configuração do Swagger/OpenAPI
│   ├── controller/
│   │   ├── AuthController.java          # Endpoint de login
│   │   └── CardController.java          # Endpoints de cartões
│   ├── dto/
│   │   ├── AuthResponse.java            # Response do login (token JWT)
│   │   ├── CardDto.java                 # DTO de cartão
│   │   ├── LoginRequest.java            # Request de login
│   │   └── UploadResultDto.java         # Response do upload de arquivo
│   ├── exception/
│   │   ├── ApiExceptionHandler.java     # Handler global de exceções
│   │   └── CardNotFoundException.java   # Exceção customizada
│   ├── model/
│   │   ├── Card.java                    # Entidade de cartão
│   │   └── User.java                    # Entidade de usuário
│   ├── repository/
│   │   ├── CardRepository.java          # Repository JPA de cartão
│   │   └── UserRepository.java          # Repository JPA de usuário
│   ├── security/
│   │   ├── JwtAccessDeniedHandler.java  # Handler de erro 403
│   │   ├── JwtAuthenticationEntryPoint.java # Handler de erro 401
│   │   ├── JwtAuthenticationFilter.java # Filtro de validação JWT
│   │   ├── JwtTokenProvider.java        # Geração e validação de tokens
│   │   └── SecurityConfig.java          # Configuração Spring Security
│   ├── service/
│   │   ├── AuthService.java             # Interface do serviço de autenticação
│   │   ├── CardService.java             # Interface do serviço de cartões
│   │   ├── CardServiceImpl.java         # Implementação do serviço de cartões
│   │   ├── FileUploadService.java       # Interface do serviço de upload
│   │   └── FileUploadServiceImpl.java   # Implementação do serviço de upload
│   └── CardApplication.java             # Classe principal
├── src/main/resources/
│   ├── application.properties           # Configurações da aplicação
│   └── DESAFIO-HYPERATIVA.txt          # ⭐ Arquivo de teste para upload
├── mvn-java17.sh                        # Script para executar com Java 17
├── pom.xml                              # Dependências Maven
└── README.md                            # Este arquivo
```

### 🎯 Principais Componentes

#### **Controllers**
- `AuthController`: Login e geração de token JWT
- `CardController`: CRUD de cartões (criar, consultar, upload)

#### **Services**
- `AuthService`: Lógica de autenticação
- `CardService`: Lógica de negócio de cartões
- `FileUploadService`: Processamento de arquivo em lote (batch de 1000 registros)

#### **Security**
- `JwtTokenProvider`: Gera e valida tokens JWT (HS256, 24h de validade)
- `JwtAuthenticationFilter`: Intercepta requisições e valida token no header
- Handlers customizados para erros 401/403 com JSON estruturado

#### **Data Initialization**
- `DataInitializer`: CommandLineRunner que cria usuários `admin` e `user` automaticamente

---

## 📦 Requisitos

### Java 17
O projeto **requer Java 17**. Não funciona com Java 21 devido à incompatibilidade do Lombok.

**Instalar Java 17:**
```bash
brew install openjdk@17
```

**Verificar versão:**
```bash
java -version
# Deve mostrar: openjdk version "17.0.x"
```

### Maven
```bash
mvn -version
```

---

## ⚙️ Configuração e Execução

### 1. Clone o Projeto
```bash
git clone <url-do-repositorio>
cd card
```

### 2. Compilar o Projeto

**Usando o script (recomendado):**
```bash
./mvn-java17.sh clean package -DskipTests
```

**Ou configurando JAVA_HOME manualmente:**
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
mvn clean package -DskipTests
```

### 3. Executar a Aplicação

**Com o script:**
```bash
./mvn-java17.sh spring-boot:run
```

**Ou diretamente:**
```bash
mvn spring-boot:run
```

### 4. Verificar Inicialização

Aguarde os logs mostrarem:
```
=== Criando usuários padrão ===
✓ Usuário 'admin' criado com sucesso
✓ Usuário 'user' criado com sucesso
=== 2 usuários criados ===
...
Started CardApplication in X seconds
```

### 5. Acessar a Aplicação

- **Base URL:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/api-docs`

---

## 🔐 Autenticação JWT

A API usa autenticação JWT (JSON Web Token). Todos os endpoints de cartões requerem autenticação.

### Usuários Pré-cadastrados

A aplicação cria automaticamente 2 usuários ao iniciar:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

### Fluxo de Autenticação

1. Fazer login em `/auth/login` para obter o token
2. Incluir o token no header `Authorization: Bearer <token>` nas requisições
3. Token válido por 24 horas

### Configurações JWT

As configurações estão em `application.properties`:

```properties
# JWT Secret (256 bits mínimo para HS256)
app.jwt.secret=hyperativaSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS256Algorithm

# Validade do token (24 horas em milissegundos)
app.jwt.expiration=86400000
```

---

## 📡 Endpoints da API

### 🔓 Público (sem autenticação)

#### **POST** `/api/auth/login` - Fazer Login

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### 🔒 Protegidos (requerem JWT)

#### **POST** `/api/cards` - Criar Cartão

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
Content-Type: application/json
```

**Request:**
```json
{
  "cardNumber": 4456897999999999
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "cardNumber": null
}
```

---

#### **GET** `/api/cards/exists` - Verificar Existência de Cartão

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
```

**Query Params:**
- `cardNumber` (obrigatório): Número do cartão

**Exemplo:** `/api/cards/exists?cardNumber=4456897999999999`

**Response (200 OK):**
```json
{
  "id": 1
}
```

**Response (404 NOT FOUND):**
```json
{
  "timestamp": "2026-02-14T20:00:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found"
}
```

---

#### **POST** `/api/cards/upload` - Upload de Arquivo em Lote

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: Arquivo TXT no formato especificado

**Response (200 OK):**
```json
{
  "loteName": "LOTE0001",
  "loteDate": "20180524",
  "declaredCount": 10,
  "processedCount": 8,
  "duplicatedCount": 2,
  "errorCount": 0,
  "errors": [],
  "status": "SUCCESS"
}
```

**Características:**
- Processamento em **batch de 1000 registros**
- Leitura **streaming** (linha por linha) para economizar memória
- Detecta **duplicados** automaticamente
- Valida formato do arquivo e quantidades
- Suporta **grandes volumes** (milhões de registros)

---

## 🧪 Testando com Swagger

### Passo a Passo Completo

1. **Acesse o Swagger UI:**
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

2. **Faça Login:**
   - Localize a seção **"Authentication"**
   - Clique em `POST /auth/login`
   - Clique em **"Try it out"**
   - No Request body, insira:
     ```json
     {
       "username": "admin",
       "password": "admin123"
     }
     ```
   - Clique em **"Execute"**
   - **Copie o token** retornado no campo `token`

3. **Autorize no Swagger:**
   - No topo da página, clique no botão **"Authorize"** 🔒
   - Na modal que abrir:
     - Campo `Value`: Digite `Bearer ` (com espaço) + cole o token
     - Exemplo: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - Clique em **"Authorize"**
   - Clique em **"Close"**

4. **Teste os Endpoints:**
   - Agora todos os endpoints com 🔒 estão autorizados
   - Experimente:
     - **POST /api/cards** - Criar um cartão
     - **GET /api/cards/exists** - Consultar cartão
     - **POST /api/cards/upload** - Upload de arquivo

---

## 📤 Upload de Arquivo

### Arquivo de Teste

O projeto inclui um arquivo de exemplo para testes de upload em:

```
src/main/resources/DESAFIO-HYPERATIVA.txt
```

**Formato do Arquivo:**

```
DESAFIO-HYPERATIVA           20180524LOTE0001000010   // Header: Nome, Data, Lote, Quantidade
C2     4456897999999999                               // Detalhe: Identificador + Número do Cartão
C1     4456897922969999                               
C3     4456897999999999                               // Duplicado (mesma numeração de C2)
...
LOTE0001000010                                        // Footer: Lote + Quantidade (validação)
```

**Estrutura:**
- **Linha 1 (Header):** Nome do arquivo, data (YYYYMMDD), lote, quantidade de registros
- **Linhas 2-N (Detalhes):** Começam com `C` seguido do número do cartão
- **Última linha (Footer):** Nome do lote + quantidade (para validação)

### Como Fazer Upload no Swagger

1. Faça login e autorize (veja seção anterior)
2. Vá em `POST /api/cards/upload`
3. Clique em **"Try it out"**
4. Clique em **"Choose File"**
5. Navegue até: `src/main/resources/DESAFIO-HYPERATIVA.txt`
6. Selecione o arquivo
7. Clique em **"Execute"**

**Resposta Esperada:**
```json
{
  "loteName": "LOTE0001",
  "loteDate": "20180524",
  "declaredCount": 10,
  "processedCount": 8,
  "duplicatedCount": 2,
  "errorCount": 0,
  "errors": [],
  "status": "SUCCESS"
}
```

### Localização do Arquivo para Upload

**Caminho Absoluto:**
```
/Users/guilhermeneto/Documents/Projetos/Estudos - Testes/Hyperativa/card/src/main/resources/DESAFIO-HYPERATIVA.txt
```

**Caminho Relativo (a partir da raiz do projeto):**
```
src/main/resources/DESAFIO-HYPERATIVA.txt
```

**Para usar em produção:**
- Coloque seus arquivos `.txt` em qualquer local
- No Swagger ou cliente HTTP, selecione o arquivo do seu sistema

---

## 💻 Exemplos com cURL

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Salvar Token em Variável
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

echo "Token: $TOKEN"
```

### 3. Criar Cartão
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":4456897999999999}'
```

### 4. Consultar Cartão
```bash
curl -X GET "http://localhost:8080/api/cards/exists?cardNumber=4456897999999999" \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Upload de Arquivo
```bash
curl -X POST http://localhost:8080/api/cards/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@src/main/resources/DESAFIO-HYPERATIVA.txt"
```

### Script Completo de Teste
```bash
#!/bin/bash

# 1. Fazer login e salvar token
echo "Fazendo login..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

echo "Token obtido: ${TOKEN:0:50}..."

# 2. Criar um cartão
echo -e "\nCriando cartão..."
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":4456897999999999}' | jq

# 3. Consultar o cartão
echo -e "\nConsultando cartão..."
curl -X GET "http://localhost:8080/api/cards/exists?cardNumber=4456897999999999" \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Upload de arquivo
echo -e "\nFazendo upload do arquivo..."
curl -X POST http://localhost:8080/api/cards/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@src/main/resources/DESAFIO-HYPERATIVA.txt" | jq

echo -e "\n✅ Testes concluídos!"
```

---

## 🛡️ Tratamento de Erros

A API retorna respostas JSON estruturadas para todos os erros:

### 401 Unauthorized (Token inválido/ausente)
```json
{
  "timestamp": "2026-02-14T20:00:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT ausente, inválido ou expirado. Faça login em /api/auth/login",
  "path": "/api/cards"
}
```

### 403 Forbidden (Sem permissão)
```json
{
  "timestamp": "2026-02-14T20:00:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Você não tem permissão para acessar este recurso",
  "path": "/api/cards"
}
```

### 404 Not Found (Cartão não existe)
```json
{
  "timestamp": "2026-02-14T20:00:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found"
}
```

---

## 🗄️ Banco de Dados

### H2 Console (Opcional)

Para inspecionar o banco de dados H2 em memória:

1. Adicione ao `application.properties`:
   ```properties
   spring.h2.console.enabled=true
   ```

2. Acesse: `http://localhost:8080/api/h2-console`

3. Configurações de conexão:
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **Username:** `sa`
   - **Password:** (deixe vazio)

4. Execute queries:
   ```sql
   SELECT * FROM users;
   SELECT * FROM cards;
   ```

---

## 📝 Notas Importantes

### Java 17 Obrigatório
- ⚠️ O projeto **não funciona com Java 21** devido à incompatibilidade do Lombok 1.18.32
- Use sempre o script `./mvn-java17.sh` ou configure `JAVA_HOME` para Java 17

### Banco em Memória
- Os dados são **perdidos ao reiniciar** a aplicação
- Para persistência, configure MySQL/PostgreSQL no `application.properties`

### Token JWT
- Validade: **24 horas**
- Algoritmo: **HS256** (HMAC with SHA-256)
- Após expirar, faça login novamente

### Usuários
- Não há endpoint de registro
- Apenas os 2 usuários pré-cadastrados (`admin` e `user`) podem fazer login

---

## 🚀 Deploy

Para deploy em produção:

1. **Build:**
   ```bash
   ./mvn-java17.sh clean package
   ```

2. **Executar JAR:**
   ```bash
   java -jar target/card-0.0.1-SNAPSHOT.jar
   ```

3. **Configurar propriedades externas:**
   ```bash
   java -jar target/card-0.0.1-SNAPSHOT.jar \
     --spring.datasource.url=jdbc:mysql://localhost:3306/carddb \
     --spring.datasource.username=user \
     --spring.datasource.password=pass \
     --app.jwt.secret=seu-secret-muito-seguro-aqui
   ```

---

## 📚 Documentação Adicional

- **Spring Security JWT:** https://spring.io/projects/spring-security
- **SpringDoc OpenAPI:** https://springdoc.org/
- **JWT.io:** https://jwt.io (para decodificar tokens)

---

## 👨‍💻 Desenvolvido por

**Hyperativa Challenge**

---

## 📄 Licença

Este projeto foi desenvolvido como parte do desafio técnico da Hyperativa.

