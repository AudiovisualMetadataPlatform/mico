![MICO](http://www.mico-project.eu/wp-content/uploads/2014/04/mico_logo.png)

[TOC]

# MICO Platform Server

The MICO Platform Server is a Linux installation providing the following services:

* an installation of Apache Marmotta with contextual extensions (found in the marmotta/ directory), running at `http://<host>:8080/marmotta`
* an installation of RabbitMQ, running at `<host>:5672`
* an FTP server for serving and storing the binary content of content items

All three services have to use the same user and password combination (for testing: `mico:mico`). 

## Debian Repository

A complete binary installation for development can be setup using custom-built packages that we offer in the MICO Debian
repository. If you want to setup a development server or virtual image, this is the easiest way to get up and running.

### 1. Setup Debian Jessie

To install these packages, first setup a basic installation of Debian **Jessie** (testing). For example, you
can start with the latest [Debian Network Installation Image](http://cdimage.debian.org/cdimage/daily-builds/daily/arch-latest/amd64/iso-cd/).
For MICO, a plain installation is sufficient, i.e. no special package preselection is needed.

Note: please do not use the username `mico`, as it will later be created by the setup process.


### 2. Add MICO Repository

Add

    deb http://apt.mico-project.eu/ mico main contrib

to your `/etc/apt/sources.list` file.

All packages are signed with a with a gpg-key (Key-ID: `AD261C57`). To avoid warnings by apt-get either install the `mico-apt-key` package or fetch the key from http://apt.mico-project.eu/apt-repo.key yourself:

    wget -O- http://apt.mico-project.eu/apt-repo.key | sudo apt-key add -


### 3. Install MICO Platform

To install the MICO platform, fetch the most recent package list and install the package `mico-platform` as follows:

    apt-get update
    apt-get install mico-platform

The installation will interactively ask you a few questions regarding a MICO user to be created and the hostname to use
for accessing the system. Please take your time to carefully configure these values. Especially, make sure you remember
the MICO password you entered.

### 4. Access MICO Platform

Web Interface:

The Debian installation comes with a single entry-point for accessing the Web interfaces of those services that provide it.
It is available at `http://<host>/`. If the server is accessible from outside the development environment, please make sure
to further protect this page by e.g. a firewall or changes to the lighttpd configuration, as it contains the login details
for the MICO user.

Sample Service:

The Debian installation also includes a sample service implemented in C++ that is used for demonstrating the platform
functionality. This service is capable of transforming text contained in JPEG and PNG images into plain text using the
tesseract OCR library. Try it out as follows:

    mico_ocr_service <host> <user> <password>

starts the service in the current terminal session. Replace <host>, <user>, and <password> with the values you provided
in the configuration phase. If you then access the broker web interface, the service and its dependencies should be shown.

Inject Content:

A simple C++ command line tool is also provided for injecting content into the platform for analysis. It can be used as follows:

    mico_inject <host> <user> <password> <files...>

where <files...> is one or more paths to files in the local file system. The call will inject a single content item, with
a content part for each file given as argument.


## VirtualBox Image

A complete installation for development is currently provided as VirtualBox image. It only has a single user "mico" with
password "mico". When starting, the server will get an IP address from VirtualBox (usually, the first IP address of the pool). 
For convenience, you can access the following administration interfaces:

* Marmotta: `http://<host>:8080/marmotta`
* RabbitMQ: `http://<host>:15672`

The FTP Server (ProFTPD) is configured to store binary data in the `/data` directory exclusively. We are currently working on
providing a more easy-to-use [vagrant](https://www.vagrantup.com) version of this image.

## Development Server

A development server will be setup once the platform stabilizes.


# MICO Platform API

This repository contains the source code of the MICO platform API and modules. It provides
implementations of the API for both Java and C++ (version 11). The API is used by analysis services to register with
the platform and by other applications to interact with the platform (e.g. inject and export content items).

## Prerequisites


### Java API

The Java API is build using Maven and will therefore retrieve all its dependencies automatically. Therefore, only the 
following prerequisites need to be satisfied:

* JDK 7
* Maven


### C++ API

Building the C++ API has additional requirements for native libraries. In particular, these are:

* GNU Autotools, GCC >= 4.8 with C++11 support
* cURL library for HTTP requests (apt-get install libcurl4-gnutls-dev)
* expat library for XML parsing (apt-get install libexpat1-dev)
* Boost 1.55 libraries for additional C++ functionalities (apt-get install libboost1.55-dev libboost-log1.55-dev libboost-system1.55-dev)
* xxd for inlining SPARQL queries in C++ (part of VIM, apt-get install vim-common)
* protobuf for the event communication protocol (manual install, the Debian/Ubuntu version is outdated)
* AMQP-CPP for communication with RabbitMQ (manual install)
* Doxygen for building the documentation

For building the C++ binary tools (mico_inject etc.), there are the following additional dependencies:

* magic library for guessing MIME type (apt-get install libmagic-dev)

For building the C++ sample analyzers (mico_ocr_service etc.), there are the following additional dependencies:

* tesseract library for OCR with English database (apt-get install libtesseract-dev tesseract-ocr-eng)
* leptonica library for image processing (apt-get install libleptonica-dev)

## Building

The API is built using the standard tools for the respective environment (i.e. Maven or GNU make). When running tests,
make sure the MICO Platform Server installation is started, and point to its host name or IP address by setting the
test.host environment variable appropriately:

    export test.host="192.168.56.101"

### Building (Java)

The complete platform is built using Maven. To build and install the current version, run

    mvn clean install

on the command line. This will compile all Java source code, run existing unit tests, build JAR
artifacts, and install them in the local Maven repository.

Binary Maven artifacts are periodically published to our development repositories:

    <repositories>
       ...
        <repository>
            <id>mico.releases</id>
            <name>MICO Relesases Repository</name>
            <url>http://mvn.mico-project.eu/content/repositories/releases/</url>
        </repository>
        <repository>
            <id>mico.snapshots</id>
            <name>MICO Snapshots Repository</name>
            <url>http://mvn.mico-project.eu/content/repositories/snapshots/</url>
        </repository>
    </repositories>

### Building (C++)

The C++ bindings of the platform are built using the CMake. To configure a build directory and create the Makefiles necessary to build the platform, create a new directory (can be located anywhere) and in that directory run 

    cmake /path/to/repository/api/c++

In case configuration succeeds (i.e. all dependencies are found), the C++ libraries can be built
and automatically tested using GNU make as follows:

    make


To create a complete API documentation of the MICO Platform API in the api/c++/doc directory, run

    make doc

To install the C++ libraries and headers to the predefined prefix, run

    make install