probation-teams is a bash script that can be used to query, update and delete functional mailboxes.
It is pre-configured to work with the 'licences-dev', 'licences-preprod' and 'licenes-prod' namespaces. 

It depends upon jq and kubectl, both of which must be on the shell PATH

The script uses kubectl to read secrets from the relevant namespace which it uses to request a token from the auth server.
