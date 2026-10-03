# API Movies

Link do vídeo: TO DO

## Descrição da Solução

A **API Movies** é uma aplicação REST desenvolvida em **Java com Spring Boot** para o gerenciamento de filmes e suas respectivas categorias.

A solução disponibiliza operações de **CRUD (Create, Read, Update e Delete)** para as entidades `Movie` e `Category`, permitindo cadastrar, consultar, atualizar e remover informações por meio de endpoints HTTP.

As entidades possuem um relacionamento **1:N (um para muitos)**, no qual uma categoria pode estar associada a vários filmes, enquanto cada filme pertence a uma categoria.

A aplicação utiliza **Spring Data JPA e Hibernate** para realizar a persistência dos dados e está integrada a um **Azure SQL Database**, utilizado como banco de dados relacional em nuvem.

A API está hospedada no **Azure App Service**, permitindo que seus endpoints sejam acessados através da internet sem depender da execução local da aplicação. O processo de publicação da aplicação é realizado por meio do **Azure CLI**, utilizando o comando `az webapp deploy`.

Para evitar a exposição de informações sensíveis no código-fonte, os dados necessários para conexão com o banco de dados, como URL, usuário e senha, são configurados através de **variáveis de ambiente no Azure App Service**.

A solução também utiliza **Azure Application Insights** para monitoramento da aplicação, possibilitando acompanhar requisições, desempenho, dependências, exceções, traces e logs gerados durante sua execução.

### Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- Azure App Service
- Azure SQL Database
- Azure Application Insights
- Azure CLI
- Insomnia

### Arquitetura da solução

De forma resumida, o fluxo da aplicação ocorre da seguinte maneira:

`Cliente (Insomnia) → Azure App Service → Spring Boot API → Azure SQL Database`

O **Application Insights** está integrado ao Azure App Service para coletar dados de telemetria e permitir o monitoramento da aplicação.

A API possui dois recursos principais:

- **Categories:** gerenciamento das categorias dos filmes.
- **Movies:** gerenciamento dos filmes e associação de cada filme a uma categoria.

Com essa arquitetura, a solução mantém a aplicação e o banco de dados hospedados na plataforma Microsoft Azure, permitindo acesso remoto à API, persistência dos dados em nuvem e monitoramento dos serviços.

# How-To — Implantação da API Movies na Microsoft Azure

Este documento apresenta o passo a passo para criação da infraestrutura, configuração do banco de dados, implantação da aplicação Spring Boot e configuração do monitoramento utilizando Microsoft Azure.

A solução utiliza os seguintes serviços:

- Azure Resource Group
- Azure SQL Server
- Azure SQL Database
- Azure App Service Plan
- Azure Web App
- Azure Application Insights
- Azure CLI

A aplicação foi desenvolvida utilizando Java 17, Spring Boot, Spring Data JPA e Hibernate.

---

## 1. Pré-requisitos

Antes de iniciar a implantação, é necessário possuir:

- Uma conta Microsoft Azure ativa;
- Azure CLI;
- Java 17;
- Maven;
- PowerShell com `Invoke-Sqlcmd`, caso a criação das tabelas seja realizada por ele;
- Projeto Spring Boot compilando corretamente.

Realize a autenticação no Azure CLI:

```bash
az login
```

Verifique a assinatura ativa:

```bash
az account show --output table
```

---

# 2. Criação do Resource Group

Execute no Azure Cloud Shell:

```bash
az group create \
  --name rg-sql-563409 \
  --location chilecentral
```

O Resource Group `rg-sql-563409` será utilizado para armazenar os recursos da solução.

---

# 3. Registrar o provider do Azure SQL

Execute:

```bash
az provider register --namespace Microsoft.Sql
```

O comando registra o provider necessário para criação e gerenciamento dos recursos do Azure SQL.

---

# 4. Criar o Azure SQL Server

Execute:

```bash
az sql server create \
  --name sql-server-rm563409-chilecentral \
  --resource-group rg-sql-563409 \
  --location chilecentral \
  --admin-user user-563409 \
  --admin-password "<SUA_SENHA_SQL>" \
  --enable-public-network true
```

> **Importante:** substitua `<SUA_SENHA_SQL>` pela senha definida para o administrador do SQL Server. Não armazene a senha real no repositório GitHub.

---

# 5. Criar o Azure SQL Database

Execute:

```bash
az sql db create \
  --resource-group rg-sql-563409 \
  --server sql-server-rm563409-chilecentral \
  --name db-563409 \
  --service-objective Basic \
  --backup-storage-redundancy Local \
  --zone-redundant false
```

O banco utilizado pela aplicação será:

```text
db-563409
```

---

# 6. Configurar regra de Firewall

Para permitir o acesso ao Azure SQL durante o desenvolvimento e os testes:

```bash
az sql server firewall-rule create \
  --resource-group rg-sql-563409 \
  --server sql-server-rm563409-chilecentral \
  --name liberaGeral \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255
```

> Esta configuração libera um intervalo amplo de endereços IP e foi utilizada para fins acadêmicos e de demonstração. Em um ambiente de produção, recomenda-se restringir o acesso apenas aos endereços e serviços necessários.

---

# 7. Criação das tabelas

A aplicação possui duas entidades relacionadas:

- `category`
- `movie`

O relacionamento é de **1:N**, em que uma categoria pode possuir vários filmes e cada filme pertence a uma categoria.

No PowerShell, execute:

```powershell
Invoke-Sqlcmd `
    -ServerInstance "sql-server-rm563409-chilecentral.database.windows.net" `
    -Database "db-563409" `
    -Username "user-563409" `
    -Password "<SUA_SENHA_SQL>" `
    -Query @"

IF NOT EXISTS (
    SELECT * FROM sysobjects
    WHERE name = 'category' AND xtype = 'U'
)
BEGIN
    CREATE TABLE category (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255)
    );
END;

IF NOT EXISTS (
    SELECT * FROM sysobjects
    WHERE name = 'movie' AND xtype = 'U'
)
BEGIN
    CREATE TABLE movie (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title VARCHAR(255),
        synopsis VARCHAR(255),
        rating INT,
        release_date DATE,
        category_id BIGINT,

        CONSTRAINT FK_movie_category
            FOREIGN KEY (category_id)
            REFERENCES category(id)
    );
END;

"@
```

Após a execução, podem ser realizadas consultas para verificar a criação das tabelas:

```sql
SELECT * FROM category;
```

```sql
SELECT * FROM movie;
```

---

# 8. Configuração da conexão JDBC

A cadeia de conexão JDBC disponibilizada pelo Azure SQL possui o seguinte formato:

```text
jdbc:sqlserver://sql-server-rm563409-chilecentral.database.windows.net:1433;database=db-563409;user=<USUARIO>;password=<SENHA>;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

Na aplicação, usuário e senha não são armazenados diretamente na URL.

A URL utilizada pela aplicação é:

```text
jdbc:sqlserver://sql-server-rm563409-chilecentral.database.windows.net:1433;database=db-563409;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

As credenciais são fornecidas separadamente através de variáveis de ambiente.

O arquivo `application.properties` utiliza:

```properties
spring.application.name=movies

spring.datasource.url=${AZURE_SQL_URL}
spring.datasource.username=${AZURE_SQL_USERNAME}
spring.datasource.password=${AZURE_SQL_PASSWORD}

spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Dessa forma, nenhuma credencial precisa ser armazenada diretamente no código-fonte.

---

# 9. Criar o Azure App Service Plan

No Azure Cloud Shell, execute:

```bash
az appservice plan create \
  --name plan-movies-rm563409 \
  --resource-group rg-sql-563409 \
  --location chilecentral \
  --sku B1 \
  --is-linux
```

Para verificar o App Service Plan:

```bash
az appservice plan show \
  --name plan-movies-rm563409 \
  --resource-group rg-sql-563409 \
  --output table
```

---

# 10. Criar o Azure Web App

Execute:

```bash
az webapp create \
  --resource-group rg-sql-563409 \
  --plan plan-movies-rm563409 \
  --name movies-rm563409 \
  --runtime "JAVA:17-java17"
```

O Web App será responsável por executar a aplicação Spring Boot.

---

# 11. Verificar o Web App

Execute:

```bash
az webapp show \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --query "{name:name,state:state,host:defaultHostName,location:location}" \
  --output table
```

Para verificar o runtime configurado:

```bash
az webapp config show \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --query linuxFxVersion
```

A aplicação utiliza Java 17.

---

# 12. Configurar as variáveis de ambiente do Azure SQL

As informações de conexão com o banco são configuradas no Azure Web App através de Application Settings.

Execute:

```bash
az webapp config appsettings set \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --settings \
  AZURE_SQL_URL="jdbc:sqlserver://sql-server-rm563409-chilecentral.database.windows.net:1433;database=db-563409;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;" \
  AZURE_SQL_USERNAME="user-563409" \
  AZURE_SQL_PASSWORD="<SUA_SENHA_SQL>"
```

As seguintes variáveis são utilizadas:

```text
AZURE_SQL_URL
AZURE_SQL_USERNAME
AZURE_SQL_PASSWORD
```

> A senha real não deve ser adicionada aos scripts armazenados no GitHub.

Essas variáveis são lidas pelo `application.properties` durante a inicialização da aplicação.

---

# 13. Gerar o arquivo JAR

Na máquina local, abra o terminal na raiz do projeto e execute:

```bash
mvn clean package -DskipTests
```

Caso esteja utilizando Maven Wrapper no Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Após a compilação, o arquivo será gerado dentro do diretório:

```text
target/
```

Exemplo:

```text
target/movies-0.0.1-SNAPSHOT.jar
```

---

# 14. Autenticação no Azure CLI local

Para realizar o deploy diretamente do computador onde o arquivo JAR foi gerado:

```bash
az login
```

Verifique a conta ativa:

```bash
az account show --output table
```

---

# 15. Deploy da aplicação com Azure CLI

Na raiz do projeto, execute:

```bash
az webapp deploy \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --src-path target/movies-0.0.1-SNAPSHOT.jar \
  --type jar
```

O comando envia o arquivo JAR gerado pelo Maven para o Azure Web App.

Após a conclusão do deploy, a aplicação ficará disponível através do Azure App Service.

Para consultar o hostname:

```bash
az webapp show \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --query defaultHostName \
  --output tsv
```

A aplicação pode ser acessada através de:

```text
https://movies-rm563409.azurewebsites.net
```

---

# 16. Testar a aplicação publicada

Os endpoints podem ser testados utilizando Insomnia, Postman ou outro cliente HTTP.

Exemplo:

# Requisições CRUD — Insomnia

A API pode ser testada utilizando o **Insomnia** através da URL publicada no Azure App Service.

## URL Base

```text
https://movies-rm563409.azurewebsites.net
```

A API possui dois recursos principais:

- `categories` — gerenciamento de categorias;
- `movies` — gerenciamento de filmes.

---

# 1. CRUD de Categories

## 1.1. Criar uma categoria — POST

### Requisição

```http
POST https://movies-rm563409.azurewebsites.net/categories
```

### Body

Selecione no Insomnia:

```text
Body → JSON
```

Utilize:

```json
{
  "name": "Ação"
}
```

### Resposta esperada

**Status: `201 Created`**

Exemplo:

```json
{
  "id": 1,
  "name": "Ação"
}
```

---

## 1.2. Listar todas as categorias — GET

### Requisição

```http
GET https://movies-rm563409.azurewebsites.net/categories
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
[
  {
    "id": 1,
    "name": "Ação"
  }
]
```

---

## 1.3. Buscar categoria por ID — GET

### Requisição

```http
GET https://movies-rm563409.azurewebsites.net/categories/1
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
{
  "id": 1,
  "name": "Ação"
}
```

Caso o ID informado não exista:

```http
GET https://movies-rm563409.azurewebsites.net/categories/999
```

### Resposta esperada

```text
404 Not Found
```

---

## 1.4. Atualizar uma categoria — PUT

### Requisição

```http
PUT https://movies-rm563409.azurewebsites.net/categories/1
```

### Body

```json
{
  "name": "Ação e Aventura"
}
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
{
  "id": 1,
  "name": "Ação e Aventura"
}
```

---

## 1.5. Excluir uma categoria — DELETE

### Requisição

```http
DELETE https://movies-rm563409.azurewebsites.net/categories/1
```

### Resposta esperada

```text
204 No Content
```

> Uma categoria que possui filmes associados não pode ser excluída. Nesse caso, a API retorna `409 Conflict`.

Exemplo:

```text
409 Conflict
```

A categoria deve ser excluída somente após os filmes associados serem removidos ou alterados para outra categoria.

---

# 2. CRUD de Movies

Antes de cadastrar um filme, é necessário possuir uma categoria cadastrada, pois cada filme deve estar associado a uma categoria.

Por exemplo, considerando a categoria:

```json
{
  "id": 1,
  "name": "Ação"
}
```

podemos cadastrar um filme utilizando o `id` dessa categoria.

---

## 2.1. Criar um filme — POST

### Requisição

```http
POST https://movies-rm563409.azurewebsites.net/movies
```

### Body

Selecione no Insomnia:

```text
Body → JSON
```

Utilize:

```json
{
  "title": "Matrix",
  "synopsis": "Um programador descobre a verdade sobre sua realidade.",
  "rating": 10,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 1
  }
}
```

### Resposta esperada

**Status: `201 Created`**

Exemplo:

```json
{
  "id": 1,
  "title": "Matrix",
  "synopsis": "Um programador descobre a verdade sobre sua realidade.",
  "rating": 10,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 1,
    "name": "Ação"
  }
}
```

---

## 2.2. Listar todos os filmes — GET

### Requisição

```http
GET https://movies-rm563409.azurewebsites.net/movies
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
[
  {
    "id": 1,
    "title": "Matrix",
    "synopsis": "Um programador descobre a verdade sobre sua realidade.",
    "rating": 10,
    "releaseDate": "1999-03-31",
    "category": {
      "id": 1,
      "name": "Ação"
    }
  }
]
```

---

## 2.3. Buscar filme por ID — GET

### Requisição

```http
GET https://movies-rm563409.azurewebsites.net/movies/1
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
{
  "id": 1,
  "title": "Matrix",
  "synopsis": "Um programador descobre a verdade sobre sua realidade.",
  "rating": 10,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 1,
    "name": "Ação"
  }
}
```

Caso o ID não exista:

```http
GET https://movies-rm563409.azurewebsites.net/movies/999
```

### Resposta esperada

```text
404 Not Found
```

---

## 2.4. Atualizar um filme — PUT

Antes deste exemplo, considere que exista outra categoria:

```json
{
  "id": 2,
  "name": "Ficção Científica"
}
```

### Requisição

```http
PUT https://movies-rm563409.azurewebsites.net/movies/1
```

### Body

```json
{
  "title": "Matrix",
  "synopsis": "Um hacker descobre que a realidade em que vive é uma simulação.",
  "rating": 9,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 2
  }
}
```

### Resposta esperada

**Status: `200 OK`**

Exemplo:

```json
{
  "id": 1,
  "title": "Matrix",
  "synopsis": "Um hacker descobre que a realidade em que vive é uma simulação.",
  "rating": 9,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 2,
    "name": "Ficção Científica"
  }
}
```

---

## 2.5. Excluir um filme — DELETE

### Requisição

```http
DELETE https://movies-rm563409.azurewebsites.net/movies/1
```

### Resposta esperada

```text
204 No Content
```

Após a exclusão, uma nova consulta:

```http
GET https://movies-rm563409.azurewebsites.net/movies/1
```

deverá retornar:

```text
404 Not Found
```

---

# 3. Validações da API

Além das operações CRUD, a API realiza validações relacionadas à associação entre filmes e categorias.

## 3.1. Cadastrar filme sem categoria

### Requisição

```http
POST https://movies-rm563409.azurewebsites.net/movies
```

### Body

```json
{
  "title": "Matrix",
  "synopsis": "Um programador descobre a verdade sobre sua realidade.",
  "rating": 10,
  "releaseDate": "1999-03-31"
}
```

### Resposta esperada

```text
400 Bad Request
```

A categoria é obrigatória para o cadastro de um filme.

---

## 3.2. Cadastrar filme com categoria inexistente

### Requisição

```http
POST https://movies-rm563409.azurewebsites.net/movies
```

### Body

```json
{
  "title": "Matrix",
  "synopsis": "Um programador descobre a verdade sobre sua realidade.",
  "rating": 10,
  "releaseDate": "1999-03-31",
  "category": {
    "id": 999
  }
}
```

### Resposta esperada

```text
404 Not Found
```

A API não permite associar um filme a uma categoria inexistente.

---

## 3.3. Excluir categoria que possui filmes

Caso uma categoria esteja associada a um ou mais filmes:

```http
DELETE https://movies-rm563409.azurewebsites.net/categories/1
```

### Resposta esperada

```text
409 Conflict
```

A categoria somente poderá ser excluída após não possuir mais filmes associados.

---

# 4. Ordem recomendada para demonstração do CRUD

Para demonstrar corretamente o relacionamento entre as entidades, recomenda-se executar as operações na seguinte ordem:

```text
1. POST   /categories
2. GET    /categories
3. GET    /categories/{id}
4. PUT    /categories/{id}

5. POST   /movies
6. GET    /movies
7. GET    /movies/{id}
8. PUT    /movies/{id}

9. DELETE /movies/{id}
10. DELETE /categories/{id}
```

Após as operações de criação, atualização e exclusão, os dados podem ser conferidos diretamente no Azure SQL Database utilizando:

```sql
SELECT * FROM category;
```

```sql
SELECT * FROM movie;
```

Dessa forma, é possível validar que as operações realizadas através da API estão sendo persistidas corretamente no Azure SQL Database.

---

# 17. Criar o Application Insights

Para monitorar a aplicação, crie um recurso Application Insights.

No Azure Cloud Shell:

```bash
az monitor app-insights component create \
  --app appi-movies-rm563409 \
  --location chilecentral \
  --resource-group rg-sql-563409 \
  --kind web \
  --application-type web
```

Verifique a criação:

```bash
az monitor app-insights component show \
  --app appi-movies-rm563409 \
  --resource-group rg-sql-563409 \
  --query "{name:name,location:location,kind:kind}" \
  --output table
```

---

# 18. Conectar o Application Insights ao Web App

Execute:

```bash
az monitor app-insights component connect-webapp \
  --resource-group rg-sql-563409 \
  --app appi-movies-rm563409 \
  --web-app movies-rm563409
```

---

# 19. Obter a Connection String do Application Insights

No Azure Cloud Shell utilizando Bash:

```bash
AI_CONNECTION_STRING=$(az monitor app-insights component show \
  --app appi-movies-rm563409 \
  --resource-group rg-sql-563409 \
  --query connectionString \
  --output tsv)
```

Para verificar se a variável foi carregada:

```bash
echo "$AI_CONNECTION_STRING"
```

---

# 20. Configurar o agente do Application Insights

Configure a Connection String e o agente do Application Insights no Web App:

```bash
az webapp config appsettings set \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --settings \
  APPLICATIONINSIGHTS_CONNECTION_STRING="$AI_CONNECTION_STRING" \
  ApplicationInsightsAgent_EXTENSION_VERSION="~3"
```

---

# 21. Verificar as Application Settings

Para visualizar os nomes das configurações existentes:

```bash
az webapp config appsettings list \
  --resource-group rg-sql-563409 \
  --name movies-rm563409 \
  --query "[].name" \
  --output table
```

Entre as configurações deverão existir:

```text
AZURE_SQL_URL
AZURE_SQL_USERNAME
AZURE_SQL_PASSWORD
APPLICATIONINSIGHTS_CONNECTION_STRING
ApplicationInsightsAgent_EXTENSION_VERSION
```

---

# 22. Reiniciar o Azure Web App

Após configurar o Application Insights:

```bash
az webapp restart \
  --resource-group rg-sql-563409 \
  --name movies-rm563409
```

Aguarde a inicialização da aplicação e realize novas requisições HTTP para gerar telemetria.

---

# 23. Verificar o Application Insights

No Azure Portal, acesse:

```text
Application Insights
→ appi-movies-rm563409
```

As áreas de monitoramento podem ser utilizadas para analisar:

- Requests;
- Performance;
- Dependencies;
- Exceptions;
- Traces;
- Logs.

---

# 24. Arquitetura final

Após a conclusão do processo, a arquitetura da solução é:

<img width="1774" height="887" alt="Arquitetura Azure para API Spring Boot" src="https://github.com/user-attachments/assets/6dc9cb0f-95b3-48c1-ae28-9bf6d89c3019" />

Todos os principais componentes da solução são executados em serviços da Microsoft Azure, enquanto as credenciais utilizadas pela aplicação são fornecidas através das configurações do Azure Web App e não ficam armazenadas diretamente no código-fonte.
