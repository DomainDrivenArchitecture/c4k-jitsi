#!/bin/bash

set -exo pipefail

function main() {
    {
        upgradeSystem
    } > /dev/null

    install -m 0700 /tmp/install-debug.sh /usr/local/bin/
    install -m 0644 /tmp/settings-config.js /defaults/settings-config.js

    cleanupDocker
}

source /tmp/install_functions_debian.sh
DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes main