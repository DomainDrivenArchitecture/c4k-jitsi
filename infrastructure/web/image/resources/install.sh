#!/bin/bash

set -exo pipefail

function main() {
    {
        apt-get update
        apt-get -qqy upgrade 
    } > /dev/null

    install -m 0700 /tmp/install-debug.sh /usr/local/bin/
    install -m 0644 /tmp/settings-config.js /defaults/settings-config.js

    cleanupDocker
}

source /tmp/install_functions.sh
main