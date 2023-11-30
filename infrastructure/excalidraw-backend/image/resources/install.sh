#!/bin/bash
set -eux

apt-get update > /dev/null
apt-get upgrade -y > /dev/null
apt-get clean

npm install -g npm@latest
npm ci --omit=dev
npm run build
