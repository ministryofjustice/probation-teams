# probation-teams
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fprobation-teams)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#probation-teams "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/probation-teams/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/probation-teams)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://probation-teams-dev.prison.service.justice.gov.uk/swagger-ui/index.html)

Probation Team Contact and Reference Service

There is a command line script at `scripts/probation-teams.sh` that can be used to test the service.

Run the script without any arguments for some help text.

In 'dev' you can exercise all the endpoints like so:
```
# list the FMBs in the N02 probation area
./probation-teams.sh -ns dev -pa N02

# list the FMBs in ldu N02All in probation area N02
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# update an ldu FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -update b@c.com

# check it
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# delete the FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -delete

# check
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# add the FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -update a@b.com

# update a team's FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -team N02AZR -update b@c.com

# check
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# delete
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -team N02AZR -delete

# check
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# add
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -team N02AZR -update a@b.com

# check
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL
```
Pipe the 'GET' output into jq for something more readable.

You must have suitable kubernetes access to the cluster namespace for this to work. (licences-dev in this instance)

The script `scripts/smoke-dev.sh` encapsulates these steps, but you must confirm the output manually.

To run:

```
./smoke-dev.sh
```