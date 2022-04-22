#!/bin/bash
set -Eeo pipefail

apt update > /dev/null

install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0600 /tmp/turnserver.conf /etc/coturn/turnserver.conf
install -m 0700 /tmp/entrypoint.sh /entrypoint.sh
