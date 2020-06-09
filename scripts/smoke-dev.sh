# A very simple smoke test for probation-teams in the dev environment...
#
# list the FMBs in the N02 probation area
PA=N02
LDU=N02ALL
TEAM=N02AZR
EMAIL1=a@b.com
EMAIL2=b@c.org

view() {
  ./probation-teams.sh -ns dev -pa $PA -ldu $LDU | jq
}

echo "List the FMBs in probation area $PA"
./probation-teams.sh -ns dev -pa $PA | jq

echo
echo "List the FMBs in probation area/ldu $PA/$LDU"
view

echo
echo "Update the FMB for ldu $PA/$LDU to '$EMAIL2'"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -update $EMAIL2
view

echo
echo "Delete the FMB for ldu $PA/$LDU"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -delete
view

echo
echo "Add the FMB '$EMAIL1' to ldu $PA/$LDU"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -update a@b.com
view

echo
echo "Update the FMB for team $PA/$LDU/$TEAM to '$EMAIL2'"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -team $TEAM -update $EMAIL2
view

echo
echo "DELETE the FMB for team $PA/$LDU/$TEAM"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -team $TEAM -delete
view

echo
echo "Add the FMB '$EMAIL1' to team $PA/$LDU/$TEAM"
./probation-teams.sh -ns dev -pa $PA -ldu $LDU -team $TEAM -update a@b.com
view