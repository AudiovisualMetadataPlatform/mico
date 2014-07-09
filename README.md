# MICO Platform

This repository contains the source code of the MICO platform and modules. It provides
implementations for both Java and C++ (version 11).

## Prerequisites

* JDK (7 or 8?)
* Maven
* ...

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

### Persistence

In parallel you may require some persistence infrastructure, which uses HDFS.
[HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html)
(Hadoop Distributed File System) is used int he MICO platform to store content. 
A [development enviroment](http://wiki.apache.org/hadoop/HowToSetupYourDevelopmentEnvironment)
requires some particular details that we try to automatize as much as possible.

    cd persistence
    mvn clean package -Phadoop

And run the HDFS daemons:

    ./start-hdfs.sh

