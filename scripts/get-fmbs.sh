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
PROBATION_AREA_CODES_TMP=$(mktemp /tmp/probation-area-codes.XXXXXX)
FMB_JSON_TMP=$(mktemp /tmp/functional-mailboxes-json.XXXXXX)
DATE_TIME=$(date +'%F_%H-%M')
OUTPUT_FILE=functional-mailboxes-${DATE_TIME}.csv
NAMESPACE=prod

echo "Extracting Probation Area Codes to $PROBATION_AREA_CODES_TMP"
./probation-teams.sh -ns $NAMESPACE -pacs | jq -r '.[]' > "$PROBATION_AREA_CODES_TMP"

echo "Extracting Functional Mailbox JSON to $FMB_JSON_TMP:"

while IFS="" read -r p || [ -n "$p" ]
do
  echo "Probation area $p"
  ./probation-teams.sh -ns $NAMESPACE -pa "$p" >> "$FMB_JSON_TMP"
done < "$PROBATION_AREA_CODES_TMP"

echo "Converting JSON to CSV format"

echo 'Probation Area code, LDU code, Probation Team code, Functional Mailbox address' > "$OUTPUT_FILE"
jq -r '
 . as $all
 | (
     paths(scalars)
     | select(.[4] == "functionalMailbox")
     | [
         $all.localDeliveryUnits[.[1]].probationAreaCode,
         $all.localDeliveryUnits[.[1]].localDeliveryUnitCode,
         .[3],
         $all.localDeliveryUnits[.[1]].probationTeams[.[3]].functionalMailbox
       ]
   ),
   (
     $all.localDeliveryUnits[]
     | select(.functionalMailbox )
     | [
         .probationAreaCode,
         .localDeliveryUnitCode,
         null,
         .functionalMailbox
       ]
   )
 | @csv' < "$FMB_JSON_TMP" | sort >> "$OUTPUT_FILE"

 echo "Output in $OUTPUT_FILE"