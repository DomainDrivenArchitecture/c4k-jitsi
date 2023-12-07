#!/bin/bash
set -exo pipefail

function main() {
    {
        apt-get update
        apt-get -qqy upgrade
    } > /dev/null
    
    cleanupDocker
}

source ./install_functions.sh
main

npm ci --omit=dev
npm run build
