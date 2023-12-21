#!/bin/bash
set -exo pipefail

function main() {
    {
        upgradeSystem
    } > /dev/null
    
    cleanupDocker
}

source ./install_functions_debian.sh
DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes main

npm ci --omit=dev
npm run build
