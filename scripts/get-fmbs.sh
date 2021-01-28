#!/usr/bin/env bash
#
# This script extracts functional mailbox addresses from the production probation teams service
# and presents them in a file in CSV format.
#
# The script retrieves the set of probation area codes known to the probation teams service.
# It invokes the probation-teams.sh script for each probation area code and concatenates the JSON outputs.
# Finally the script uses jq to extract the functional mailbox addresses in CSV format, outputting the
# results to a file
#
# Each line of the CSV output consists of
# probation area code, ldu code, team code or blank, functional mailbox address.
#
# To run this script successfully you must be able to use the probations-teams.sh script against the production environment.
# See notes for that script.
# You must also have the command-line program jq installed.
#
FMB_JSON_TMP=$(mktemp /tmp/functional-mailboxes-json.XXXXXX)
DATE_TIME=$(date +'%F_%H-%M')

usage() {
  echo
  echo "Usage:"
  echo
  echo " command line parameters:"
  echo
  echo "   -ns <namespace>            One of 'dev', 'preprod' or 'prod'. Selects the kubernetes namespace. "
  echo
  echo "  Examples:"
  echo
  echo "  get-fmbs -ns dev"
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
  dev | preprod | prod) ;;

  *)
    echo "-ns must be 'dev', 'preprod' or 'prod'"
    exit
    ;;
  esac
}

get_fmb_json() {
  echo "Extracting Functional Mailbox JSON to $FMB_JSON_TMP:"
  ./probation-teams.sh -ns "$NS_KEY" -fmbs >>"$FMB_JSON_TMP"
}

convert_to_csv() {
  echo "Converting JSON to CSV format"
  OUTPUT_FILE=functional-mailboxes-${NS_KEY}_${DATE_TIME}.csv

  echo 'Probation Area code, LDU code, Probation Team code, Functional Mailbox address' >"$OUTPUT_FILE"
  jq -r '
 . as $all
 | (
     paths(scalars)
     | select(.[3] == "functionalMailbox")
     | [
         $all[.[0]].probationAreaCode,
         $all[.[0]].localDeliveryUnitCode,
         .[2],
         $all[.[0]].probationTeams[.[2]].functionalMailbox
       ]
   ),
   (
     $all[]
     | select(.functionalMailbox )
     | [
         .probationAreaCode,
         .localDeliveryUnitCode,
         null,
         .functionalMailbox
       ]
   )
 | @csv' <"$FMB_JSON_TMP" | sort >>"$OUTPUT_FILE"

  echo "Output in $OUTPUT_FILE"
}

read_command_line "$@"
check_namespace
get_fmb_json
convert_to_csv
