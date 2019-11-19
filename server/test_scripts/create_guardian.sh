#!/bin/sh

if [ $# -lt 2 ]
then
    echo "Usage: $0 username password"
    exit 1
fi
curl --header "Content-Type: application/json" \
  -w " : %{http_code}" \
  --request POST \
  --data '{"username":"'"$1"'","password":"'"$2"'"}' \
  http://"${IP:-localhost}":6894/guardian/create
echo
