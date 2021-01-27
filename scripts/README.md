### probation-teams.sh

probation-teams.sh is a bash script that can be used to query, update and delete functional mailboxes.
It is pre-configured to work with the kubernetes namespaces 'licences-dev', 'licences-preprod' and 'licenes-prod'. 
The script depends upon jq and kubectl, both of which must be on the shell PATH .  The script uses kubectl to read 
secrets from the relevant namespace so you must have access to these namespaces through kbuectl.

Running the script without any arguments for usage instructions.

### get-fmbs.sh

get_fmbs.sh retrieves all functional mailbox addresses from the licences-dev, licences-preprod or licences-prod namspace
The results are output to a file in CSV format. Each row in the file contains:
* Probation Area code
* Local Delivery Unit code
* Probation Team code if the functional mailbox is attached to a probation team, otherwise empty
* functional mailbox address

Run the script without any arguments for usage instructions.

### smoke-dev.sh
smoke-dev.sh is a bash script that uses probation-teams.sh and get-fmbs.sh to exercise the probation teams service
in the dev environment. You must confirm the output of this script manually.
