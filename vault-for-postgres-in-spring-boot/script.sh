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
  VAULT_ROOT_TOKEN=$(echo "${VAULT_KEYS}" | jq -r .root_token)

  echo
  echo "--> unsealing Vault ..."
  curl -X PUT -d '{ "key": "'${VAULT_KEY1}'" }' ${VAULT_ADDR}/v1/sys/unseal

  echo
  echo "--> Vault token: ${VAULT_ROOT_TOKEN}"
  echo "--> Vault status"
  curl ${VAULT_ADDR}/v1/sys/init

}

VAULT_ROOT_TOKEN=s.s9TTmbSQPahlg5MtfMdmjQF8

enableAppRole() {

  echo
  echo "================"

  echo
  echo "--> enabling the AppRole auth method ..."
  curl -X POST -i -H "X-Vault-Token: ${VAULT_ROOT_TOKEN}" -d '{"type": "approle"}' ${VAULT_ADDR}/v1/sys/auth/approle

}

enableKVSecretsEngine() {

  echo "================"
  echo "-- KV Secrets Engine - Version 1"

  echo
  echo "--> enabling KV Secrets Engine ..."
  curl -X POST -i -H "X-Vault-Token: ${VAULT_ROOT_TOKEN}" \
    -d '{"type": "kv", "description": "Spring Boot KV Secrets Engine", "config": {"force_no_cache": true}}' \
    ${VAULT_ADDR}/v1/sys/mounts/secret

}

enableDatabasePlugin() {

  echo "================"
  echo "-- Mounting Database ..."
  curl -X POST -i -H "X-Vault-Token:${VAULT_ROOT_TOKEN}" -d '{"type": "database"}' ${VAULT_ADDR}/v1/sys/mounts/database

  echo "--> configuring Postgres plugin and connection ..."
  curl \
    --header "X-Vault-Token: ${VAULT_ROOT_TOKEN}" \
    --request POST \
    --data @docker/vault/postgresql-database-plugin.json \
    ${VAULT_ADDR}/v1/database/config/postgresql

}

simulateGetOrders() {

  while true; do
    curl -I http://localhost:8080/api/customers/CUST0001/students
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
  enableAppRole
  enableKVSecretsEngine
  enableDatabasePlugin
  ;;
--simulate)
  simulateGetOrders
  ;;
--destroy)
  docker-compose rm -f -s -v
  ;;
*)
  echo "$(basename ${0}):usage: [--infra, --unseal, --setup, --simulate, --destroy]"
  exit 1 # Command to come out of the program with status 1
  ;;
esac
