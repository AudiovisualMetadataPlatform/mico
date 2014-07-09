# MICO Platform

This repository contains the source code of the MICO platform and modules. It provides
implementations for both Java and C++ (version 11).

## Prerequisites

* JDK (7 or 8?)
* Maven
* ...

### Prerequisites (C++)

Building the C++ API has additional requirements for native libraries. In particular, these are:

* GNU Autotools, GCC >= 4.8 with C++11 support
* cURL library for HTTP requests (apt-get install libcurl4-gnutls-dev)
* expat library for XML parsing (apt-get install libexpat1-dev)
* Boost 1.55 libraries for additional C++ functionalities (apt-get install libboost1.55-dev)
* xxd for inlining SPARQL queries in C++ (part of VIM, apt-get install vim-common)
* Hadoop native libraries (manual install, see below)

Building Hadoop Native:

This process is described in detail on the [Hadoop Website](http://hadoop.apache.org/docs/r2.4.1/hadoop-project-dist/hadoop-common/NativeLibraries.html). 
Quick summary:

* download Hadoop 2.x source release and unpack in a custom directory
* `mvn package -Pdist,native -DskipTests -Dtar`

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

    ./configure --prefix=/usr/local --enable-testing --with-hadoop=PATH

Where PATH is the location where the Hadoop distribution has been 

In case configuration succeeds (i.e. all dependencies are found), the C++ libraries can be built
and automatically tested using GNU make as follows:

    make

To run C++ unit tests for the persistence API, startup the platform as described below and then call

    make check

To install the C++ libraries and headers to the predefined prefix, run

    make install
   
In this case, you would probably also want to make sure that the Hadoop Native Libraries are properly 
installed in the system before.

## Launching

To launch the different webapps, run

    mvn package tomcat7:run

### Persistence

@@TODO@@: marmotta and all other subsystems

In parallel you may require some persistence infrastructure, which uses HDFS.
[HDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html)
(Hadoop Distributed File System) is used int he MICO platform to store content. 
A [development enviroment](http://wiki.apache.org/hadoop/HowToSetupYourDevelopmentEnvironment)
requires some particular details that we try to automatize as much as possible.

    cd persistence
    mvn clean package -Phadoop

And run the HDFS daemons:

    ./start-hdfs.sh

And the you can access it at [localhost:50070](http://localhost:50070/)

To shutdown HDFS just use Ctrl+C in the active terminal.

