# OCPP Station Simulator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[GitHub](https://github.com/evbox/station-simulator) repository is readonly, for contribution please use [GitLab](https://gitlab.com/evbox/open-source/station-simulator)

## Introduction

The simulator tool is designed to simulate charging station. It implements [OCPP protocol](https://en.wikipedia.org/wiki/Open_Charge_Point_Protocol) and talks to Charging Station Management 
System (CSMS) via WebSocket protocol.

## Pre-requisites
* JRE 8 or above
* A fully running charging station management system (CSMS)

## Installation

```bash
$ git clone git@gitlab.com:evbox/open-source/station-simulator.git
$ cd station-simulator/
$ ./gradlew build
```

If you wish to include the simulator in your project as a library:

### Maven

Add the following to your project `POM` file:
```xml

<dependency>
    <groupId>io.everon</groupId>
    <artifactId>ocpp-station-simulator</artifactId>
    <version>0.5.4</version>
    <type>pom</type>
</dependency>
```

### Gradle

Add the following to your `build.gradle` file
```groovy
compile 'io.everon:ocpp-station-simulator:0.5.4'
```

## Build

To build:

```bash
$ ./gradlew build
```

Build fat jar that can be executed without gradle or source code
```shell
./gradlew clean fatJar
```
Copy jar from `simulator-core/build/libs/simulator-core-0.5.16-all.jar`  

## Publish
```shell
./gradlew publish
```

## Usage

Starts the simulator with one station, which has a single EVSE and a single connector attached to it:

```bash
 $ ./gradlew run -Parguments="ws://{ocpp_endpoint_url} --configuration {'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1}}]}"
````
To start the simulator with a certificate installed on startup, follow the following steps:
- generate keypair and save the private/public keys as **private.key/public.key** under the same directory and provide the path to the station configuration using property named **keyPairPath**
- using the generated keypair, create a csr, sign it be your CA, then provide the path of your certificate to the station configuration using property named **manufacturerCertificatePath**
```bash
 $ ./gradlew run -Parguments="ws://{ocpp_endpoint_url} --configuration {'stations':[{'id':'EVB-P17390866','evse':{'count':1,'connectors':1},'manufacturerCertificatePath':'{path_to_certificate}','keyPairPath':'{path_to_key_pair}'}]}"
````

To start standalone jar file use
```shell
java -jar {path_to_jar_file} ws://{ocpp_endpoint_url} --configuration "{'stations':[{'id':'{station_identity_code}','evse':{'count':1,'connectors':1},'keyPairPath':'{path_to_key_pair}','manufacturerCertificatePath':'{path_to_certificate_chain}','hardwareConfiguration':{'serialNumber':'{station_hardware_serial_number}'}}]}"
```
### Guide for Instant charging transaction with Everon

Follow [Readme](docs/autostart/Autostart.md) for detailed steps

## Bugs and Feedback

For bugs, questions and discussions please check for any existing issues created under
the *[GitLab Issues](https://gitlab.com/evbox/open-source/station-simulator/-/issues)* section.
