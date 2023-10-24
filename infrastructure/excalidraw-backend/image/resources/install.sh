#!/bin/bash
set -Eeo pipefail

apt-get update > /dev/null
apt-get upgrade -y > /dev/null
apt-get clean

npm install -g npm
npm ci
npm run build
