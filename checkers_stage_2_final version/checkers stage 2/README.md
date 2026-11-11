# Checkers Multiplayer
Peer-to-Peer Gaming with Java Networking (Socket Programming)

## Overview
![Overview](https://github.com/tk-codes/checkers/blob/master/Documentation/overview.png)
![Server-Client](https://github.com/tk-codes/checkers/blob/master/Documentation/server_client.PNG)

## Class Diagram
###Server
![Server Class Diagram](https://github.com/tk-codes/checkers/blob/master/Checkers%20-%20Server/doc/ServerSide.png)

### Client
![Client Class Diagram](https://github.com/tk-codes/checkers/blob/master/Checkers%20-%20Client/doc/ClientSide.png)

## How to get started
###1. Server Configuration

> **Checkers - Server \resources\config.properties**

set the TCP port
```
port = 50800
```

start the server application
```
Checkers - Server \src\ServerMain.java
```

###2. Client Configuration

> **Checkers - Client \resources\config.properties**

set the server ip address and tcp port
```
server=127.0.0.1
port=50800
```

start the client application
```
Checkers - Client \src\ClientMain.java
```

## GUI - Client (Player)
![Play](https://github.com/tk-codes/checkers/blob/master/Documentation/play.PNG)

## Application Logic
![App Logic](https://github.com/tk-codes/checkers/blob/master/Documentation/app_logic.PNG)


## Stage 2 - Structural Patterns

### Adapter Pattern - AI Hint
The client has a **Hint** button. The hint comes from an external Minimax AI
([sramakrishnan247/Checkers-AI](https://github.com/sramakrishnan247/Checkers-AI), MIT License),
whose interface does not match our game, so it is used through an adapter.

| Role (chapter) | Chapter example | This project |
|---|---|---|
| Target | `EnemyAttacker` | `Hint/HintProvider` |
| Adaptee | `EnemyRobot` | `ExternalAI/AI` (external code, unchanged) |
| Adapter | `EnemyRobotAdapter` | `Hint/AIHintAdapter` |
| Client | `Client` | `Handler/Controller` |

The external files are in `Checkers - Client/src/ExternalAI`. The only change to them is the
`package ExternalAI;` line, which is needed so that classes in named packages can use them.
