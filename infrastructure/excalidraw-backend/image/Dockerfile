# Taken from: https://github.com/jitsi/excalidraw-backend
FROM node:20.10.0-bookworm-slim

WORKDIR /excalidraw-backend

COPY resources/package.json resources/package-lock.json resources/tsconfig.json resources/install.sh resources/src resources/install_functions.sh ./

RUN DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes ./install.sh

EXPOSE 80
EXPOSE 9090

CMD ["npm", "start"]