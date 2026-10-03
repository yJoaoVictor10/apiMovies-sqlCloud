# API Movies

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
