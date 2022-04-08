#!/bin/bash
set -Eeo pipefail

apt update > /dev/null

install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0755 /tmp/entrypoint.sh /