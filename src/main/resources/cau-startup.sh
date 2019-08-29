#!/bin/bash 
# script to set up server certificate and java key and truststores
# for the CAU server.
# The script uses openssl and java keytool.
# It is intended to run inside the CAU container.
# Author: Shirley Crompton, UKRI-STFC
# Last modified: 29 August 2019

printf '\e[0;33m %-15s \e[0m start setting up CAU....\n' "[CAU-startup]"

function logError {
    msg=$1
    printf '\e[0;33m %-15s \e[0;31m Error:\e[0m %s \n' "[CAU-startup]" "${msg}"
}

#store trusted ca endpoint env variable.
caep="$TRUSTCA"

if [ -z "${caep}" ]; then
    logError "No CA endpoint, cannot continue..."
    exit 1
fi

#credential folder in the container
fp="/pkidata/cau/"

#########
#check if we need to get credentials
if [ -e "$fp"server.p12 ] && [ -e "$fp"cau-server.jks ] && [ -e "$fp"clientTrust.jks ]; then
    echo "..already got credentials.  Going directly to lauch cau server...."
else
    #get ca cert and also the reciprical untrusted ca
    echo "About to get CA cert from ${caep}..."
    curl -kv "${caep}"  > "${fp}"trust.pem

    utep="${caep/trustedca/untrustedca}"

    echo "About to get CA cert from the reciprical untrusted ca (${utep})..."
    curl -kv "${utep}"  > "${fp}"untrust.pem

    #check if the pems are in place
    if [ ! -e "${fp}"trust.pem ] || [ ! -e "${fp}"untrust.pem ]; then
        logError "Missing trust certificates, cannot proceed..."
        exit 1
    fi

    #hostname command should return container id
    hname="cau-"`hostname`
    echo "cau hostname: ${hname}"

    #get certificate from it2 trustedca, ca generates key & cert
    echo "About to get cau cert from ${caep}..."
    curl -kv -XPOST "${caep}" --data-binary "${hname}" -H "Content-type: text/plain" > "${fp}"cau.pem

    pem=`cat "${fp}"cau.pem`

    if [ -z "${pem}"  ]; then 
        logError "Failed to get cert, cannot proceed..."
        exit 1
    #else
    #    echo " credential pem : ${pem} "
    fi

    #convert to p12
    openssl pkcs12 -export -in "${fp}"cau.pem -inkey "${fp}"cau.pem -out "${fp}"server.p12 -name server -CAfile "${fp}"trust.pem -caname root -passin pass:serverStore -passout pass:serverStore

    if [ ! -e "${fp}"server.p12 ]; then
        logError "Failed to export credentials to p12 format, cannot proceed ..."
        exit 1
    fi

    #set up key stores for the CA clent handshake use
    #keystore
    keytool -importkeystore -srckeystore "${fp}"server.p12 -srcstoretype pkcs12 -destkeystore "${fp}"cau-server.jks -deststoretype JKS -storepass serverStore -keypass serverStore -srcstorepass serverStore -noprompt

    if [ ! -e "${fp}"cau-server.jks ]; then
        logError "Failed to create cau-server.jks, cannot proceed ..."
        exit 1
    fi

    #truststore
    keytool -import -file "${fp}"trust.pem -alias it2trusted -keystore "${fp}"clientTrust.jks -deststoretype JKS -storepass trustStore -noprompt
    keytool -import -file "${fp}"untrust.pem -alias it2ut -keystore "${fp}"clientTrust.jks -storepass trustStore -noprompt


    if [ ! -e "${fp}"clientTrust.jks ]; then
        logError "Failed to create clientTrust.jks, cannot proceed ..."
        exit 1
    fi
fi

############
#launch the application
cd /var/app/
echo "Launching CAU server ....."
"${JAVA_HOME}"/bin/java -jar cau.jar --cloudca="${caep}"
