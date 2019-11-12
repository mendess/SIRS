#!/bin/sh

if [ $# -lt 2 ]
then
    echo "Usage: $0 guardian_id username"
    exit 1
fi

curl --header "Content-Type: application/json" \
  -w " : %{http_code}" \
  --request POST \
  --data '{"guardian":'"$1"',"username":"'"$2"'"}' \
  http://localhost:8000/child/create \
  | jq
echo
