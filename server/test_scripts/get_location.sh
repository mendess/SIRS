#!/bin/sh

if [ $# -lt 2 ]
then
    echo "Usage: $0 guardian_id child_id"
    exit 1
fi

curl --header "Content-Type: application/json" \
  -w " : %{http_code}" \
  --request POST \
  --data '{"guardian":'"$1"',"child":'"$2"'}' \
  http://localhost:8000/guardian \
  | jq
echo
