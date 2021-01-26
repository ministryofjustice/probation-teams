#!/usr/bin/env bash
#
# This script extracts functional mailbox addresses from the production probation teams service
# and presents them the file fmbs.csv in CSV format.
#
# The script takes a list of probation area codes, one per line from probation-areas.txt
# It invokes the probation-teams.sh script for each probation area code and concatenates the JSON outputs in
# tmp.json
# Finally the script uses jq to extract the functional mailbox addresses in CSV format, outputting the
# results to fmbs.csv
#
# Each line of the CVS output consists of
# probation area code, ldu code, team code or blank, functional mailbox address.
#
# To run this script successfully you must be able to use the probations-teams.sh script
# against the production environment. See notes for that script.
# You must also have the command-line program jq installed.
#
TEMPFILE=$(mktemp /tmp/probation-teams-output.XXXXXX)
DATE_TIME=$(date +'%F_%T')
OUTPUT_FILE=fmbs-${DATE_TIME}.csv

echo "Extracting JSON data for probation area codes to $TEMPFILE:"

while IFS="" read -r p || [ -n "$p" ]
do
  echo "Probation area $p"
  ./probation-teams.sh -ns prod -pa "$p" >> $TEMPFILE
done < probation-areas.txt

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
 | @csv' < "$TEMPFILE" | sort >> "$OUTPUT_FILE"

 echo "Output in $OUTPUT_FILE"