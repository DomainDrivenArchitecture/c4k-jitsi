#!/bin/bash
set -exo pipefail

apt-get update > /dev/null
apt-get -y upgrade > /dev/null
apt-get clean
rm -rf /var/lib/apt/lists/*

npm ci --omit=dev
npm run build
