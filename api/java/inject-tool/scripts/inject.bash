#!/usr/bin/env bash

# usage: inject FILE ROUTEID

# set -x




SUBMIT_ENDPOINT="http://mico-platform:8080/broker/inject/submit"
CREATE_ENDPOINT="http://mico-platform:8080/broker/inject/create"



JSON_PY=$(cat <<-END
import json
with open("tmpinjectinfo") as file:
   itemInfo = json.load(file)
   print(itemInfo["itemUri"])
END
)


echo "Uploading" $1

curl --progress -X POST --data-binary \"@$1\" ${CREATE_ENDPOINT} > tmpinjectinfo

ITEM_URI=$(python -c "${JSON_PY}")

echo "Submitting" ${ITEM_URI}

curl -X POST -v ${SUBMIT_ENDPOINT}?route=$2\&item=${ITEM_URI}