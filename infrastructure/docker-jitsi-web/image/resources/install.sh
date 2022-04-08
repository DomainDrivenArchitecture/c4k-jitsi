#!/bin/bash
set -Eeo pipefail

apt update > /dev/null

install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0644 /tmp/settings-config.js /defaults/settings-config.js