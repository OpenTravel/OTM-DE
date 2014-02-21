#!/bin/bash
bold="\033[1m"
endbold="\033[0m"
GREEN="\e[0;32m"
RESET="\e[0m"

if [ $# -eq 0 ]; then
    echo "Usage: relese.sh <NewcopmilerVersion=3.1> <NewPDEVersion=3.1.1>"
    exit 1
fi
NEW_COMPILER_VERSION=$1
NEW_PLUGIN_VERSION=$2


PARENT_POM="pom.xml"
CURRENT_VERSION=`sed -n -e 's/.*<version>\(.*\)<\/version>.*/\1/p' ${PARENT_POM} | head -n 1`
CURRENT_COMPILER_VERSION=`sed -n -e 's/.*<compiler\.version>\(.*\)<\/compiler\.version>/\1/p' ${PARENT_POM}`

echo -e "Current Compiler version: ${GREEN}${CURRENT_COMPILER_VERSION}${RESET}"
echo -e "Current OTM-DE version: ${GREEN}${CURRENT_VERSION}${RESET}"
echo -e "New version: ${GREEN}${NEW_VERSION}${RESET}"

sed -i "s/<compiler\.version>.*<\/compiler\.version>/<compiler\.version>${NEW_COMPILER_VERSION}<\/compiler\.version>/g" ${PARENT_POM}


echo -e "${bold}Update plugin verions${RESET}"
mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:0.17.0:set-version -DnewVersion=${NEW_PLUGIN_VERSION}

echo -e "${bold}Update compiler version in parent pom.xml${RESET}"
sed -i "s/<compiler\.version>.*<\/compiler\.version>/<compiler\.version>${NEW_COMPILER_VERSION}<\/compiler\.version>/g" ${PARENT_POM}

echo -e "${bold}Update p2-local-site version${RESET}"
sed -i "s/<version>${CURRENT_VERSION}<\/version>/<version>${NEW_PLUGIN_VERSION}<\/version>/" ./target-definition/local-p2-site/pom.xml

echo -e "${bold}Update ota2 dependencies to schema-compiler-ext-ota2${RESET}"
sed -i "s/schema-compiler-ext-ota2-${CURRENT_COMPILER_VERSION}/schema-compiler-ext-ota2-${NEW_COMPILER_VERSION}/g" ./plugins/org.opentravel.schemas.stl2Developer.ota2/build.properties
sed -i "s/schema-compiler-ext-ota2-${CURRENT_COMPILER_VERSION}/schema-compiler-ext-ota2-${NEW_COMPILER_VERSION}/g" ./plugins/org.opentravel.schemas.stl2Developer.ota2/META-INF/MANIFEST.MF

echo -e "${bold}Update schemacopmiler MANIFEST.MF${RESET}"
sed -i "s/schema-compiler-${CURRENT_COMPILER_VERSION}/schema-compiler-${NEW_COMPILER_VERSION}/g" ./plugins/org.opentravel.schemas.stl2Developer.schemacompiler/META-INF/MANIFEST.MF

