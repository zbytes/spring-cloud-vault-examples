#!/usr/bin/env bash
set -e

VAULT_ADDR=http://localhost:8200

launch() {

  echo "================"
  echo
  echo "--> Running in docker"
  docker-compose up -d
  watch "docker-compose ps"

}

unsealVault() {

  echo "================"
  echo "-- Initializing Vault"

  VAULT_KEYS=$(curl -X PUT -s -d '{ "secret_shares": 1, "secret_threshold": 1 }' ${VAULT_ADDR}/v1/sys/init | jq .)
  VAULT_KEY1=$(echo "${VAULT_KEYS}" | jq -r .keys_base64[0])
  VAULT_TOKEN=$(echo "${VAULT_KEYS}" | jq -r .root_token)

  echo
  echo "--> unsealing Vault ..."
  curl -X PUT -d '{ "key": "'"${VAULT_KEY1}"'" }' ${VAULT_ADDR}/v1/sys/unseal

  echo

  echo "--> Vault status"
  curl ${VAULT_ADDR}/v1/sys/init
  tee vault_token.log <<EOF
$VAULT_TOKEN
EOF

}

loadVaultToken() {

  VAULT_TOKEN=$(cat vault_token.log)

  echo
  echo "--> Vault token: ${VAULT_TOKEN}"
}

enableAppRole() {

  echo
  echo "================"
  echo "Enable App Role"

  echo
  echo "--> enabling the AppRole auth method ..."
  curl -X POST -i -H "X-Vault-Token: ${VAULT_TOKEN}" -d '{"type": "approle"}' ${VAULT_ADDR}/v1/sys/auth/approle
}

enableSecrets() {

  APPLICATION_NAME="vault-for-postgres-in-spring-boot"

  CUSTOM_ROLE_ID="${APPLICATION_NAME}-role-id"
  DATABASE_USER="${APPLICATION_NAME}-user"

  DATABASE_ROLE="${APPLICATION_NAME}-role"
  DATABASE_ROLE_POLICY="${APPLICATION_NAME}-policy"

  KV_ROLE_POLICY="${APPLICATION_NAME}-kv-policy"

  DATABASE="PostgreSQL"

  echo
  echo "================"
  echo "KV Secrets"
  echo
  echo "--> enabling KV Secrets ..."
  curl -X POST -i -H "X-Vault-Token: ${VAULT_TOKEN}" \
    -d '{"type": "kv", "description": "Spring Boot KV Secrets Engine", "config": {"force_no_cache": true}}' \
    ${VAULT_ADDR}/v1/sys/mounts/${APPLICATION_NAME}
  echo

  echo
  echo "================"
  echo "${DATABASE} Database Secrets"

  echo
  echo "--> mounting database ..."
  curl -X POST -i -H "X-Vault-Token:${VAULT_TOKEN}" -d '{"type": "database"}' ${VAULT_ADDR}/v1/sys/mounts/database

  echo
  echo "--> configuring ${DATABASE} plugin and connection ..."

  tee payload.json <<EOF
{
    "plugin_name": "postgresql-database-plugin",
    "connection_url": "postgresql://{{username}}:{{password}}@db:5432/postgres?sslmode=disable",
    "allowed_roles": "$DATABASE_ROLE",
    "username": "username",
    "password": "password"
}
EOF

  curl \
    --header "X-Vault-Token: ${VAULT_TOKEN}" --request POST \
    --data @payload.json \
    ${VAULT_ADDR}/v1/database/config/postgresql

  echo
  echo "--> creating database role '${DATABASE_ROLE}' ..."

  # Note: Setting the 'default_ttl' and 'max_ttl' in the command above does not work!
  # In order to test shorter times, change 'config.hcl' file.
  tee payload.json <<EOF
{
    "db_name": "postgresql",
    "creation_statements": [
    "CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}' INHERIT;",
    "GRANT ALL ON ALL TABLES IN SCHEMA public TO \"{{name}}\";"
  ],
    "default_ttl": "2m",
    "max_ttl": "5m"
}
EOF

  curl --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST --data @payload.json \
    $VAULT_ADDR/v1/database/roles/${DATABASE_ROLE}

  echo
  echo "--> setting Database policy '${DATABASE_ROLE_POLICY}' ..."
  curl -X POST -i -H "X-Vault-Token:${VAULT_TOKEN}" -d '{"policy":"path \"database/creds/'${DATABASE_ROLE}'\" {policy=\"read\"} path \"sys/renew/database/creds/*\" {capabilities=[\"update\"]}"}' ${VAULT_ADDR}/v1/sys/policy/${DATABASE_ROLE_POLICY}

  echo
  echo "--> testing Database role '${DATABASE_ROLE}' with ROOT_TOKEN ..."
  curl -i -H "X-Vault-Token:${VAULT_TOKEN}" ${VAULT_ADDR}/v1/database/creds/${DATABASE_ROLE}

  echo
  echo "--> List of leases"
  curl -i -H "X-Vault-Token: ${VAULT_TOKEN}" -X LIST ${VAULT_ADDR}/v1/sys/leases/lookup/database/creds/${DATABASE_ROLE}

  echo
  echo "--> creating AppRole '${DATABASE_USER}' with policies '${DATABASE_ROLE_POLICY}' and '${KV_ROLE_POLICY}' ..."
  curl -X POST -i -H "X-Vault-Token: ${VAULT_TOKEN}" -d '{"policies": ["'${DATABASE_ROLE_POLICY}'", "'${KV_ROLE_POLICY}'"], "bound_cidr_list": "0.0.0.0/0", "bind_secret_id": false}' ${VAULT_ADDR}/v1/auth/approle/role/${DATABASE_USER}

  echo
  echo "--> update ROLE_ID with custom value '${CUSTOM_ROLE_ID}'"
  curl -X POST -i -H "X-Vault-Token: ${VAULT_TOKEN}" -d '{"role_id": "'${CUSTOM_ROLE_ID}'"}' ${VAULT_ADDR}/v1/auth/approle/role/${DATABASE_USER}/role-id

  echo
  echo "--> fetching the identifier of the AppRole '${DATABASE_USER}' ..."
  ROLE_ID=$(curl -s -H "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/auth/approle/role/${DATABASE_USER}/role-id | jq -r .data.role_id)
  echo "ROLE_ID=${ROLE_ID}"

  echo
  echo "--> getting client token ..."
  CLIENT_TOKEN=$(curl -X POST -s -d '{"role_id":"'"${ROLE_ID}"'"}' ${VAULT_ADDR}/v1/auth/approle/login | jq -r .auth.client_token)
  echo "CLIENT_TOKEN=${CLIENT_TOKEN}"

  echo
  echo "--> testing ${DATABASE} role '${DATABASE_ROLE}' with CLIENT_TOKEN ..."
  curl -i -H "X-Vault-Token:${CLIENT_TOKEN}" ${VAULT_ADDR}/v1/database/creds/${DATABASE_ROLE}
  echo

}

runApp() {
  cd .. && ./gradlew :vault-for-postgres-in-spring-boot:bootRun
}

simulateGetOrders() {

  while true; do
    curl -I http://localhost:8080/api/customers/CUST0001/orders
    sleep 1
  done

}

option="${1}"

case ${option} in
--infra)
  launch
  ;;
--unseal)
  unsealVault
  ;;
--setup)
  loadVaultToken
  enableAppRole
  enableSecrets
  ;;
--run)
  runApp
  ;;
--simulate)
  simulateGetOrders
  ;;
--destroy)
  docker-compose rm -f -s -v
  ;;
*)
  echo "$(basename ${0}):usage: [--infra, --unseal, --setup, --run, --simulate, --destroy]"
  exit 1 # Command to come out of the program with status 1
  ;;
esac
