# Taken from: https://github.com/jitsi/excalidraw-backend
FROM node:16.17-slim

WORKDIR /excalidraw-backend


COPY resources/package.json resources/package-lock.json resources/tsconfig.json resources/src ./
RUN npm install
RUN npm run build

EXPOSE 80
EXPOSE 9090

CMD ["npm", "start"]