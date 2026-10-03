#!/bin/bash

# ============================================================
# API MOVIES
# Script de criação e configuração dos recursos no Azure
# ============================================================

RESOURCE_GROUP="rg-sql-563409"
LOCATION="chilecentral"

SQL_SERVER="sql-server-rm563409-chilecentral"
SQL_DATABASE="db-563409"
SQL_ADMIN="user-563409"

APP_SERVICE_PLAN="plan-movies-rm563409"
WEB_APP="movies-rm563409"

APP_INSIGHTS="appi-movies-rm563409"

# ============================================================
# SENHA DO AZURE SQL
# ============================================================

read -s -p "Digite a senha do Azure SQL: " SQL_PASSWORD
echo

# ============================================================
# RESOURCE GROUP
# ============================================================

az group create \
  --name "$RESOURCE_GROUP" \
  --location "$LOCATION"


# ============================================================
# PROVIDER SQL
# ============================================================

az provider register \
  --namespace Microsoft.Sql


# ============================================================
# AZURE SQL SERVER
# ============================================================

az sql server create \
  --name "$SQL_SERVER" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --admin-user "$SQL_ADMIN" \
  --admin-password "$SQL_PASSWORD" \
  --enable-public-network true


# ============================================================
# AZURE SQL DATABASE
# ============================================================

az sql db create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name "$SQL_DATABASE" \
  --service-objective Basic \
  --backup-storage-redundancy Local \
  --zone-redundant false


# ============================================================
# FIREWALL DO AZURE SQL
# ============================================================

az sql server firewall-rule create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name liberaGeral \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255


# ============================================================
# APP SERVICE PLAN
# ============================================================

az appservice plan create \
  --name "$APP_SERVICE_PLAN" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --sku B1 \
  --is-linux


# ============================================================
# AZURE WEB APP
# ============================================================

az webapp create \
  --resource-group "$RESOURCE_GROUP" \
  --plan "$APP_SERVICE_PLAN" \
  --name "$WEB_APP" \
  --runtime "JAVA:17-java17"


# ============================================================
# VARIÁVEIS DE AMBIENTE DO AZURE SQL
# ============================================================

AZURE_SQL_URL="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;database=${SQL_DATABASE};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

az webapp config appsettings set \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEB_APP" \
  --settings \
  AZURE_SQL_URL="$AZURE_SQL_URL" \
  AZURE_SQL_USERNAME="$SQL_ADMIN" \
  AZURE_SQL_PASSWORD="$SQL_PASSWORD"


# ============================================================
# APPLICATION INSIGHTS
# ============================================================

az monitor app-insights component create \
  --app "$APP_INSIGHTS" \
  --location "$LOCATION" \
  --resource-group "$RESOURCE_GROUP" \
  --kind web \
  --application-type web


# ============================================================
# CONECTAR APPLICATION INSIGHTS AO WEB APP
# ============================================================

az monitor app-insights component connect-webapp \
  --resource-group "$RESOURCE_GROUP" \
  --app "$APP_INSIGHTS" \
  --web-app "$WEB_APP"


# ============================================================
# CONNECTION STRING DO APPLICATION INSIGHTS
# ============================================================

AI_CONNECTION_STRING=$(az monitor app-insights component show \
  --app "$APP_INSIGHTS" \
  --resource-group "$RESOURCE_GROUP" \
  --query connectionString \
  --output tsv)


# ============================================================
# CONFIGURAR APPLICATION INSIGHTS NO WEB APP
# ============================================================

az webapp config appsettings set \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEB_APP" \
  --settings \
  APPLICATIONINSIGHTS_CONNECTION_STRING="$AI_CONNECTION_STRING" \
  ApplicationInsightsAgent_EXTENSION_VERSION="~3"


# ============================================================
# DEPLOY DA APLICAÇÃO
# ============================================================

az webapp deploy \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEB_APP" \
  --src-path target/movies-0.0.1-SNAPSHOT.jar \
  --type jar


# ============================================================
# REINICIAR WEB APP
# ============================================================

az webapp restart \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEB_APP"


# ============================================================
# FINALIZAÇÃO
# ============================================================

echo
echo "============================================================"
echo "Deploy concluído."
echo "URL da aplicação:"
echo "https://${WEB_APP}.azurewebsites.net"
echo "============================================================"