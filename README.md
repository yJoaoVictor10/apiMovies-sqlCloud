# HOW TO — Executar a API Movies via Docker (qualquer máquina)

Este guia permite subir toda a aplicação utilizando apenas Docker, a partir do `git clone`, incluindo:

- Banco MySQL
- API Java Spring Boot
- Rede Docker
- Persistência de dados
- Testes CRUD
- Evidências exigidas pelo professor

---

# 1. Pré-requisitos

Antes de começar, instale:

- Docker Desktop (Windows/Mac)
- Docker Engine + Docker Compose (Linux)
- Git

Verifique se tudo está funcionando:

```bash
docker --version
git --version
```

---

# 2. Clonar o repositório

```bash
git clone https://github.com/yJoaoVictor10/apiMovies-rm563409.git
```

Entrar na pasta:

```bash
cd apiMovies-rm563409
```

---

# 3. Criar rede Docker

A rede permitirá comunicação entre a API e o MySQL.

```bash
docker network create rede-rm563409
```

Verificar:

```bash
docker network ls
```

---

# 4. Criar volume persistente do MySQL

O volume garante persistência dos dados mesmo após reiniciar o container.

```bash
docker volume create mysql-volume-rm563409
```

Verificar:

```bash
docker volume ls
```

---

# 5. Subir container MySQL

```bash
docker run -d \
--name mysql-db-rm563409 \
--network rede-rm563409 \
-v mysql-volume-rm563409:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=root123 \
-e MYSQL_DATABASE=moviesdb \
-e MYSQL_USER=moviesuser \
-e MYSQL_PASSWORD=moviespass \
-p 3306:3306 \
mysql:8.4
```

Verificar se o container está rodando:

```bash
docker ps
```

Você deverá ver:

```text
mysql-db-rm563409
```

---

# 6. Criar Dockerfile da aplicação

Criar arquivo:

```bash
touch Dockerfile.api
```

Editar:

```bash
nano Dockerfile.api
```

Inserir o conteúdo abaixo:

```dockerfile
# =========================
# BUILD
# =========================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY . .

RUN mvn clean package -DskipTests


# =========================
# RUNTIME
# =========================
FROM eclipse-temurin:21-jre

RUN addgroup --system moviesgroup && \
    adduser --system --ingroup moviesgroup moviesuser

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown moviesuser:moviesgroup app.jar

USER moviesuser

EXPOSE 8080

ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db-rm563409:3306/moviesdb \
    SPRING_DATASOURCE_USERNAME=moviesuser \
    SPRING_DATASOURCE_PASSWORD=moviespass \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Salvar:

```bash
CTRL + O
ENTER
CTRL + X
```

---

# 7. Build da imagem Docker da API

```bash
docker build -f Dockerfile.api -t movies-app-rm563409 .
```

Verificar imagens:

```bash
docker images
```

Você deverá ver:

```text
movies-app-rm563409
```

---

# 8. Subir container da API Java

```bash
docker run -d \
--name movies-app-rm563409 \
--network rede-rm563409 \
-p 8080:8080 \
movies-app-rm563409
```

---

# 9. Verificar containers ativos

```bash
docker ps
```

Resultado esperado:

```text
mysql-db-rm563409
movies-app-rm563409
```

---

# 10. Verificar logs

## Logs da aplicação Java

```bash
docker logs movies-app-rm563409
```

## Logs do banco MySQL

```bash
docker logs mysql-db-rm563409
```

A API deverá iniciar sem erros.

---

# 11. Verificar rede Docker

```bash
docker network inspect rede-rm563409
```

Os dois containers devem aparecer na seção:

```json
"Containers"
```

---

# 12. Entrar no container da aplicação

O professor exige uso de `docker exec` + `whoami`.

Entrar no container:

```bash
docker exec -it movies-app-rm563409 sh
```

Executar:

```bash
whoami
pwd
ls
```

Resultado esperado do `whoami`:

```text
moviesuser
```

Sair:

```bash
exit
```

---

# 13. Acessar MySQL diretamente

Entrar no banco:

```bash
docker exec -it mysql-db-rm563409 mysql -u moviesuser -p
```

Senha:

```text
moviespass
```

Selecionar banco:

```sql
USE moviesdb;
```

Ver tabelas:

```sql
SHOW TABLES;
```

Consultar dados:

```sql
SELECT * FROM movie;
```

---

# 14. URL da API

## Localhost

Se estiver rodando localmente:

```text
http://localhost:8080
```

## Máquina virtual / nuvem

Substitua pelo IP da VM:

```text
http://SEU-IP:8080
```

Exemplo:

```text
http://20.220.222.177:8080/movies
```

---

# 15. Testes CRUD da API

---

# CREATE

Inserir registro:

```bash
curl -X POST http://localhost:8080/movies \
-H "Content-Type: application/json" \
-d '{
  "title":"Interstellar",
  "synopsis":"Space movie",
  "rating":10,
  "releaseDate":"2014-11-07"
}'
```

Verificar no banco:

```sql
SELECT * FROM movie;
```

---

# READ

Consultar registros:

```bash
curl http://localhost:8080/movies
```

---

# UPDATE

Atualizar registro:

```bash
curl -X PUT http://localhost:8080/movies/1 \
-H "Content-Type: application/json" \
-d '{
 "title":"Interstellar Updated",
 "synopsis":"Updated synopsis",
 "rating":9,
 "releaseDate":"2014-11-07"
}'
```

Verificar:

```sql
SELECT * FROM movie;
```

---

# DELETE

Excluir registro:

```bash
curl -X DELETE http://localhost:8080/movies/1
```

Verificar:

```sql
SELECT * FROM movie;
```

---

# 16. Teste de persistência

Objetivo: provar que os dados permanecem salvos após reiniciar o MySQL.

## Passo 1 — Inserir dados via API

Executar um POST.

---

## Passo 2 — Confirmar no banco

```sql
SELECT * FROM movie;
```

---

## Passo 3 — Parar MySQL

```bash
docker stop mysql-db-rm563409
```

---

## Passo 4 — Subir novamente

```bash
docker start mysql-db-rm563409
```

---

## Passo 5 — Confirmar persistência

Entrar novamente no MySQL:

```bash
docker exec -it mysql-db-rm563409 mysql -u moviesuser -p
```

Executar:

```sql
USE moviesdb;
SELECT * FROM movie;
```

Os dados devem continuar existindo.

---

# 17. Encerrar ambiente

Parar containers:

```bash
docker stop movies-app-rm563409
docker stop mysql-db-rm563409
```

Remover containers:

```bash
docker rm movies-app-rm563409
docker rm mysql-db-rm563409
```

---

# 18. Remover ambiente completamente (opcional)

Remover rede:

```bash
docker network rm rede-rm563409
```

Remover volume:

```bash
docker volume rm mysql-volume-rm563409
```

Remover imagem da API:

```bash
docker rmi movies-app-rm563409
```

---
