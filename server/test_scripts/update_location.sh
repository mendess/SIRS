#!/bin/bash

if [ $# -lt 1 ]
then
    echo "Usage: $0 child_id [latitude longitude]"
    exit 1
fi

curl --header "Content-Type: application/json" \
  -w " : %{http_code}" \
  --request POST \
  --data '{"child_id":'"$1"',"latitude":'"${2:-$RANDOM}"', "longitude": '"${3:-$RANDOM}"'}' \
  http://"${IP:-localhost}":6894/child \
  | cat
echo
