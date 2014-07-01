# MICO Platform

This repository contains the source code of the MICO platform and modules. It provides
implementations for both Java and C++ (version 11).

## Building

### Building (Java)

The complete platform is built using Maven. To build and install the current version, run

    mvn clean install

on the command line. This will compile all Java source code, run existing unit tests, build JAR
artifacts, and install them in the local Maven repository.


### Building (C++)

The C++ bindings of the platform are built using the GNU autotools. The repository only contains the
raw input files for autotools that can be transformed into the well-known configure scripts. To do
so, please run:

    aclocal && autoreconf --install

To configure the bindings for your platform, run:

    ./configure --prefix=/usr/local

In case configuration succeeds (i.e. all dependencies are found), the C++ libraries can be built
using GNU make as follows:

    make && make install

## Launching

To launch the different webapps, run

    mvn package tomcat7:run

In parallel you may require some persistence infrastructure, for wich run

    cd persistence
    mvn clean mvn clean compile hadoop:start

