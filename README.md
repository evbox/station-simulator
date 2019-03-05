OCPP Station Simulator
======================
The simulator tool is dedicated for simulation of charging station. It implements [OCPP protocol](https://en.wikipedia.org/wiki/Open_Charge_Point_Protocol) and talks to Charging Station Management 
System (CSMS) via WebSocket protocol. At the moment only OCPP 2.0 (download [here](https://www.openchargealliance.org/protocols/ocpp-20/)) is supported.

[![CircleCI](https://circleci.com/gh/evbox/station-simulator.svg?style=svg)](https://circleci.com/gh/evbox/station-simulator)

1. [Capabilities](#capabilities)
    1. [Supported OCPP messages](#supported-ocpp-messages)
    1. [Auto-reconnect](#auto-reconnect)
1. [How to run?](#how-to-run)
    1. [Configuration](#configuration)
    1. [Run as JAR file](#run-as-jar-file)
    1. [Use as console tool](#use-as-console-tool)
    1. [Use as dependency library](#use-as-dependency-library)
1. [How it works?](#how-it-works)
    1. [Design overview](#design-overview)
    1. [Dependencies](#dependencies)
1. [How to contribute?](#how-to-contribute)
    1. [Working in IDEA](#working-in-idea)

-----------

# Capabilities
## Supported OCPP messages
- BootNotification
- Heartbeat
- Authorize
- TransactionEvent
- StatusNotification
- Reset (only IMMEDIATE reset is supported)
- GetVariables (will return REJECTED status)
- SetVariables (will return ACCEPTED, but does not store variables anywhere)

## Auto-reconnect
Simulator supports reconnection logic. In case of IO interruption in underlying WebSocket client it is going to try re-establishing connection each 5 seconds infinitely.

-----------

# How to run?
In order to run simulator you should have JRE 8+ installed on your machine

## Available command line options
| Flag                                                                   | Description                                                                         |
|:-----------------------------------------------------------------------|:------------------------------------------------------------------------------------|
|```--printConfiguration``` or ```-p```                                  |indicates whether effective configuration has to be printed to console on start-up   |
|```--configurationFile {file_path}``` or ```-f```                       |specifies path to configuration in YAML format                                       |
|```--configuration {configuration_body}``` or ```-c```                  |specifies in-line configuration in JSON-like format                                  |

> **Note:**
> If both `--configurationFile` and `--configuration` are specified only the latter one will be taken into account

## Configuration

### Command-line configuration
You can run simulator by providing configuration directly using command line:

```bash
./gradlew run -Parguments="ws://${ocpp_endpoint_url} --configuration {configuration_body}"
```

Where configuration body is an inlined JSON-like configuration with single-quotes variable names.

**Sample configuration_body**

```
{'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}
``` 

**Sample usage**

Starts simulator with one station, which has single EVSE and single connector attached to it

`./gradlew run -Parguments="ws://{ocpp_endpoint_url} --configuration {'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}"`

### File-based configuration
**Configuration file sample**
```YAML
heartbeatInterval: 60
stations:
  - id: EVB-P17390866
    evse:
      count: 1
      connectors: 1
  - id: EVB-P18090564
    evse:
      count: 1
      connectors: 2
      
```

**Sample usage**

`./gradlew run -Parguments="ws://{ocpp_endpoint_url} --configurationFile ./configuration.yml"`

## Run as JAR file
Build it:
```bash
./gradlew build && tar -zxvf simulator-core/build/distributions/simulator-core-shadow.tar
```

Run with file-based configuration:
```bash
java -jar simulator-core-shadow/lib/simulator-core.jar ws://${ocpp_endpoint_url} --configurationFile ./configuration.yml
```
Run with command-line configuration:
```bash
java -jar simulator-core-shadow/lib/simulator-core.jar ws://${ocpp_endpoint_url} --configuration "{'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}"
```

## Use as console tool
There is a console tool which supports user interaction with running stations: plug cable, unplug cable, authorize token
It starts as part of station simulator.

**List of available actions**

Press `Enter` to see list of available stations.
To switch between stations enter station's number (e.g. 1, 2, 3) and press `Enter`.

`plug {evseId} {connectorId}` plug cable to given connector (e.g. `plug 2 1`)

`unplug {evseId} {connectorId}` unplug cable from given connector (e.g. `unplug 2 1`)

`auth {tokenId} {evseId}` authorize token at given evse (e.g. `auth 045918E24B4D80 1`)

`stat` show state of selected station

## Use as dependency library
Simulator can be included as a dependency to your project. We suppose this way it can be useful for creation of automated tests for CSMS.
At the moment this use-case has not been verified. You can try and share your experience with us.

-----------

# How it works?
## Design overview

![Architecture diagram of station simulator](./diagram.svg "Architecture diagram of station simulator")

Each station is run by one separate thread, has its own state, call registry and WebSocket connection.

In order to prevent concurrent change of state any interaction with station is based on message queueing.

In same manner station is using message queue to send messages to WebSocketClient, although WebSocketClient can be also accessed by other thread from underlying WebSocket library.

-----------

# How to contribute?
## Working in IDEA
1. Install JDK 8
1. Install [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) for IDEA
1. Clone project sources
1. Inside IDEA go 'File' -> 'Open...' then select the top-level build.gradle. Choose the 'Use local gradle distribution' option and select the directory you installed gradle into previously and click ok.
1. Enable annotations processing for Lombok: Open 'Preferences...' -> 'Build, Execution, Deployment' -> Compiler -> Annotation Processors -> Check 'Enable annotation processing' checkbox

## Working in other IDEs
Our primary development tool is IDEA. If you are using other IDE share your experience and propose adjustments to this README.
