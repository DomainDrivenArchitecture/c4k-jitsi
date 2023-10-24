#!/bin/bash
set -Eeo pipefail

apt-get update > /dev/null
apt-get upgrade -y > /dev/null
apt-get clean


install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0644 /tmp/settings-config.js /defaults/settings-config.js