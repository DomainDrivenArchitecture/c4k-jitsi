#!/bin/bash
set -exo pipefail

echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections

apt-get update > /dev/null
apt-get -qy upgrade > /dev/null
apt-get clean
rm -rf /var/lib/apt/lists/*


install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0644 /tmp/settings-config.js /defaults/settings-config.js