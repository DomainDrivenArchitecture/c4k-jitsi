// Taken from: https://github.com/jitsi/excalidraw-backend

import debug from 'debug';
import dotenv from 'dotenv';
import express from 'express';
import { createServer } from 'node:http';
import { Server } from 'socket.io';
/* 
import * as prometheus from 'socket.io-prometheus-metrics';

do not use anymore, since 3 years no further progression, depends on debug 4.1.1, 
wich is moderate vulnerable to regular expression denial of service when untrusted user 
input is passed into the o formatter. 

alternatively could be used prom-client
*/

const serverDebug = debug('httpServer');
const app = express();
const port = process.env.PORT || 80; // default port to listen
const httpServer = createServer(app);

dotenv.config(
  process.env.NODE_ENV === 'development'
      ? { path: '.env.development' }
      : { path: '.env.production' }
);

app.get('/', (req, res) => {
    res.send('Excalidraw backend is up :)');
});

httpServer.listen(port, () => {
    serverDebug(`listening on port: ${port}`);
});

const corsOptions = { 
                    origin: ['jitsi.test.meissa.de', 'jitsi.prod.meissa.de'],
                    methods: ["GET", "POST"],
                    credentials: true
};

const io = new Server(httpServer, {
    allowEIO3: true,
    cors: corsOptions,
    maxHttpBufferSize: 1e6,
    pingTimeout: 10000
});

// listens on host:9090/metrics
/* do not use
prometheus.metrics(io, {
    collectDefaultMetrics: true
});
*/

/* alternatively could be used:

const client = require('prom-client');
const collectDefaultMetrics = client.collectDefaultMetrics;
const Registry = client.Registry;
const register = new Registry();
collectDefaultMetrics({ register });

or more:
https://codersociety.com/blog/articles/nodejs-application-monitoring-with-prometheus-and-grafana
*/

io.on('connection', (socket) => {
    serverDebug(`connection established! ${socket.conn.request.url}`);
    io.to(`${socket.id}`).emit('init-room');
    socket.on('join-room', roomID => {
        serverDebug(`${socket.id} has joined ${roomID} for url ${socket.conn.request.url}`);
        socket.join(roomID);
        if (io.sockets.adapter.rooms.get(roomID)?.size ?? 0 <= 1) {
            io.to(`${socket.id}`).emit('first-in-room');
        } else {
            socket.broadcast.to(roomID).emit('new-user', socket.id);
        }
        io.in(roomID).emit(
            'room-user-change', Array.from(io.sockets.adapter.rooms.get(roomID) ?? [])
        );
    });

    socket.on(
    'server-broadcast',
        (roomID: string, encryptedData: ArrayBuffer, iv: Uint8Array) => {
            socket.broadcast.to(roomID).emit('client-broadcast', encryptedData, iv);
    });

    socket.on(
    'server-volatile-broadcast',
    (roomID: string, encryptedData: ArrayBuffer, iv: Uint8Array) => {
        socket.volatile.broadcast
        .to(roomID)
        .emit('client-broadcast', encryptedData, iv);
    });

    socket.on('disconnecting', () => {
        const rooms = io.sockets.adapter.rooms;

        for (const roomID of Object.keys(socket.rooms)) {
            const clients = Array.from(rooms.get(roomID) ?? []).filter(id => id !== socket.id);
                        
            if (roomID !== socket.id) {
                socket.to(roomID).emit('user has left', socket.id);
            }

            if (clients.length > 0) {
                socket.broadcast.to(roomID).emit('room-user-change', clients);
            }
        }
    });

    socket.on('disconnect', (reason, details) => {
        serverDebug(
            `${socket.id} was disconnected from url ${socket.conn.request.url} for the following reason: ${reason}
            ${JSON.stringify(details)}`
        );
        socket.removeAllListeners();
    });
});
