OCPP Station Simulator
======================

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[ ![Download](https://api.bintray.com/packages/everon/maven/ocpp-station-simulator/images/download.svg) ](https://bintray.com/everon/maven/ocpp-station-simulator/_latestVersion)


## Introduction
The simulator tool is dedicated for simulation of charging station. It implements [OCPP protocol](https://en.wikipedia.org/wiki/Open_Charge_Point_Protocol) and talks to Charging Station Management 
System (CSMS) via WebSocket protocol. At the moment only OCPP 2.0 (download [here](https://www.openchargealliance.org/protocols/ocpp-20/)) is supported.

# Usage
In order to run the simulator you should have JRE 8+ installed on your machine.

For the release notes and version numbers check the  [CHANGELOG.md](https://github.com/evbox/station-simulator/blob/master/CHANGELOG.md) page

## Maven

To use the simulator in Maven-based project, use the following dependency:
``` xml
<dependency>
  <groupId>io.everon</groupId>
  <artifactId>ocpp-station-simulator</artifactId>
  <version>0.3.4</version>
  <type>pom</type>
</dependency>
```

## Gradle

For Gradle add this to your build file:
```
compile 'io.everon:ocpp-station-simulator:0.3.4'
```

## Build

To build:
``` Bash
$ git clone https://github.com/evbox/station-simulator.git 
$ cd station-simulator/
$ ./gradlew build
```

## Run
Starts the simulator with one station, which has a single EVSE and a single connector attached to it:

`./gradlew run -Parguments="ws://{ocpp_endpoint_url} --configuration {'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}"`

For the complete documentation refer to the wiki.

## Full Documentation
See the [Wiki](https://github.com/evbox/station-simulator/wiki) for full documentation, examples, operational details and other information.

## Bugs and Feedback
For bugs, questions and discussions please use the GitHub Issues.
