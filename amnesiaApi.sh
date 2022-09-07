#!/bin/bash

error=""
check_path () {
	if [ -e "$1" ]; then
		error+=""
	else
		error+="$1 not exists.\n"
	fi

}

# HTTPS_URL="https://amnesia.openaire.eu/amnesia/anonymizedata"
HTTPS_URL="http://localhost:8181/anonymizedata"
CURL_CMD="curl -s -w httpcode=%{http_code}"

dataset=""
template=""
hiers=""
pathout=""
del=""
exportpath=""

while [ "$1" != "" ]; do
    if [ "$1" == "-d" ]; then
    	shift
    	check_path $1
    	dataset=" --form files=@$1 "
    elif [ "$1" == "-t" ]; then
    	shift
    	check_path $1
    	template=" --form files=@$1 "
    elif [ "$1" == "--out" ]; then
    	shift
    	pathout="--out $1"
    	exportpath="$1"
    elif [ "$1" == "-del" ]; then
    	shift
    	del=" --form del=$1"
    else
    	check_path $1
    	hiers+=" --form files=@$1 "
    fi

    # Shift all the parameters down by one
    shift

done

# echo "$dataset $template $hiers $pathout"
# echo "$CURL_CMD  $dataset $template $hiers $del $pathout $HTTPS_URL"
if [ "$error" != "" ]; then
	echo -e "$error"
	exit 2
fi

CURL_RETURN_CODE=0
CURL_OUTPUT=`${CURL_CMD} ${dataset} ${template} ${hiers} ${del} ${pathout} ${HTTPS_URL} 2> /dev/null` || CURL_RETURN_CODE=$?

httpCode=$(echo "${CURL_OUTPUT}" | sed -e 's/.*\httpcode=//')
if [[ ${httpCode} -ne "" ]] && [[ ${httpCode} -ne 200 ]]; then
    # echo "Curl operation/command failed due to server return code - ${httpCode}"
    if [ -e  "$exportpath" ]; then
    	cat "$exportpath"
    else
    	echo "Httpcode is $httpcode"
    fi

    exit 1
fi

if [ ${CURL_RETURN_CODE} -ne 0 ]; then
    echo "Curl connection failed with return code - ${CURL_RETURN_CODE}"
else
	if [ "$template" == "" ]; then
    	echo "Template was downloaded successfully in $exportpath"
    else
    	echo "The dataset was anonymized successfully and the file was downloaded in $exportpath"
    fi
fi
