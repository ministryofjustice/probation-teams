#!/usr/bin/env bash

declare -A AUTH_URL
declare -A PROBATION_TEAMS_URL

# DEV
AUTH_URL['dev']=https://gateway.t3.nomis-api.hmpps.dsd.io/auth
PROBATION_TEAMS_URL['dev']=https://probation-teams-dev.prison.service.justice.gov.uk

# PREPROD
AUTH_URL['preprod']=https://gateway.preprod.nomis-api.service.hmpps.dsd.io/auth
PROBATION_TEAMS_URL['preprod']=https://probation-teams-preprod.prison.service.justice.gov.uk

# PROD
AUTH_URL['prod']=https://gateway.prod.nomis-api.service.hmpps.dsd.io/auth
PROBATION_TEAMS_URL['prod']=https://probation-teams.prison.service.justice.gov.uk

usage() {
  echo
  echo "Usage:"
  echo
  echo " command line parameters:"
  echo
  echo "   -ns <namespace>            One of 'dev', 'preprod' or 'prod'. Selects the kubernetes namespace. "
  echo "                              If 'prod' is selected the user is asked to confirm the operation"
  echo "   -pa <probation area code>  Required"
  echo "   -ldu <LDU code>            Optional"
  echo "   -team <team code>          Required when a team functional mailbox is to be updated or deleted"
  echo "   -update <email address>    PUT the email address to the selected LDU or team functional mailbox"
  echo "   -delete                    DELETE the selected LDU or team functional mailbox"
  echo
  echo "  Examples:"
  echo
  echo "  GET the resource(s) for probation area 'N02':"
  echo "  probation-teams -ns dev -pa N02"
  echo
  echo "  GET the LDU resource for probation area 'N02' and LDU 'YSNYOR':"
  echo "  probation-teams -ns dev -pa N02 -ldu YSNYOR"
  echo
  echo "  Update the functional mailbox for N02/YSNYOR:"
  echo "  probation-teams -ns dev -pa N02 -ldu YSNYOR -update <abc@def.org>"
  echo
  echo "  DELETE the functional mailbox at N02/YSNYOR:"
  echo "  probation-teams -ns dev -pa N02 -ldu YSNYOR -delete"
  echo
  echo "  Update a team's functional mailbox:"
  echo "  probation-teams -ns dev -pa N02 -ldu YSNYOR -team T1 -update <pqr@def.org>"
  echo
  echo "  DELETE a team's functional mailbox"
  echo "  probation-teams -ns dev -pa N02 -ldu YSNYOR -team T1 -delete"
  echo
  exit
}

read_command_line() {
  if [[ ! $1 ]]; then
    usage
  fi
  while [[ $1 ]]; do
    case $1 in
    -ns)
      shift
      NS_KEY=$1
      ;;
    -pa)
      shift
      PROBATION_AREA_CODE=$1
      ;;
    -ldu)
      shift
      LOCAL_DELIVERY_UNIT_CODE=$1
      ;;
    -team)
      shift
      TEAM_CODE=$1
      ;;
    -update)
      shift
      COMMAND=update
      EMAIL=$1
      ;;
    -delete)
      COMMAND=delete
      ;;
    -help)
      usage
      ;;
    *)
      echo
      echo "Unknown argument '$1'"
      echo
      exit
      ;;
    esac
    shift
  done
}

check_namespace() {
  case "$NS_KEY" in
  dev | preprod | prod)
    NAMESPACE=licences-${NS_KEY}
    ;;
  *)
    echo "-ns must be 'dev', 'preprod' or 'prod'"
    exit
    ;;
  esac
}

set_base_url() {
  if [[ ! $PROBATION_AREA_CODE ]]; then
    echo "Probation area code not set."
    exit
  fi

  if [[ ! $LOCAL_DELIVERY_UNIT_CODE ]]; then
    BASE_URL=${PROBATION_TEAMS_URL[$NS_KEY]}/probation-areas/${PROBATION_AREA_CODE}
  elif [[ $TEAM_CODE ]]; then
    BASE_URL=${PROBATION_TEAMS_URL[$NS_KEY]}/probation-areas/${PROBATION_AREA_CODE}/local-delivery-units/${LOCAL_DELIVERY_UNIT_CODE}/teams/${TEAM_CODE}
  else
    BASE_URL=${PROBATION_TEAMS_URL[$NS_KEY]}/probation-areas/${PROBATION_AREA_CODE}/local-delivery-units/${LOCAL_DELIVERY_UNIT_CODE}
  fi
}

set_auth_header() {
  local NOMIS_AUTH_URL=${AUTH_URL[$NS_KEY]}
  local SECRETS="$(kubectl -n ${NAMESPACE} get secret licences -o json)"
  local API_SECRET=$(echo "${SECRETS}" | jq -r ".data.ADMIN_API_CLIENT_SECRET | @base64d")
  local API_ID=$(echo "${SECRETS}" | jq -r ".data.ADMIN_API_CLIENT_ID | @base64d")
  local AUTH_BASIC=$(echo -n ${API_ID}:${API_SECRET} | base64 -b0)
  local TOKEN=$(curl -s -X POST "${NOMIS_AUTH_URL}/oauth/token?grant_type=client_credentials" -H 'Content-Type: application/json' -H 'Content-Length: 0' -H "Authorization: Basic ${AUTH_BASIC}" | jq -r '.access_token')
  AUTH_HEADER="Authorization: Bearer $TOKEN"
}

confirm() {
  if [[ ${NS_KEY} == "prod" ]]; then
    local confirm=""
    echo "This is the PRODUCTION namespace"
    while [[ ${confirm} != "Y" ]] && [[ ${confirm} != "n" ]]; do
      read -p 'Continue? Y/n: ' -n 1 confirm
      echo
    done
    if [[ ${confirm} != "Y" ]]; then
      exit
    fi
  fi
}

get_ldu_or_probation_area() {
  if [[ $TEAM_CODE ]]; then
    echo "Don't use '-team' with GET"
    exit
  fi
  echo "GET ${BASE_URL}"
  curl -s -H "${AUTH_HEADER}" -X HEAD ${BASE_URL} -w 'HTTP status: %{http_code}\n'
  curl -s -H "${AUTH_HEADER}" -X GET ${BASE_URL} | jq
}

update_mailbox() {
  if [[ $EMAIL ]]; then
    echo "PUT ${EMAIL}"
    echo "  to ${BASE_URL}/functional-mailbox"
    confirm
    curl -s -H "${AUTH_HEADER}" -X PUT ${BASE_URL}/functional-mailbox -H 'Content-Type: application/json' -d \"${EMAIL}\" -w 'HTTP status: %{http_code}\n'
  else
    echo "Missing email address for -update"
    exit
  fi
}

delete_mailbox() {
  echo "DELETE ${BASE_URL}/functional-mailbox"
  confirm
  curl -s -H "${AUTH_HEADER}" -X DELETE ${BASE_URL}/functional-mailbox -w 'HTTP status: %{http_code}\n'

}

do_command() {
  case $COMMAND in
  update)
    update_mailbox
    ;;
  delete)
    delete_mailbox
    ;;
  *)
    get_ldu_or_probation_area
    ;;
  esac
}

read_command_line "$@"
check_namespace
set_base_url
set_auth_header
do_command
