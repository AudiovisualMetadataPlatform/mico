# MICO Platform Server

The MICO Platform Server is a Linux installation providing the following services:

* an installation of Apache Marmotta with contextual extensions (found in the marmotta/ directory), running at http://<host>:8080/marmotta
* an installation of RabbitMQ, running at <host>:5672
* an FTP server for serving and storing the binary content of content items

All three services have to use the same user and password combination (for testing: "mico"/"mico"). 

## VirtualBox Image

A complete installation for development is currently provided as VirtualBox image. It only has a single user "mico" with
password "mico". When starting, the server will get an IP address from VirtualBox (usually, the first IP address of the pool). 
For convenience, you can access the following administration interfaces:

* Marmotta: http://<host>:8080/marmotta
* RabbitMQ: http://<host>:15672

The FTP Server (ProFTPD) is configured to store binary data in the /data directory exclusively. We are currently working on
providing a more easy-to-use "vagrant" version of this image.

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


### Building (C++)

The C++ bindings of the platform are built using the GNU autotools. The repository only contains the
raw input files for autotools that can be transformed into the well-known configure scripts. To do
so, please run:

    cd api/c++
    aclocal && autoreconf --install

To configure the bindings for your platform, run:

    ./configure --prefix=/usr/local --enable-testing 

Where PATH is the location where the Hadoop distribution has been installed (e.g. `/usr/local/src/hadoop-2.4.1-src/hadoop-dist/target/hadoop-2.4.1`)

In case configuration succeeds (i.e. all dependencies are found), the C++ libraries can be built
and automatically tested using GNU make as follows:

    make

To run C++ unit tests for the persistence API, startup the platform as described below and then call

    make check


To create a complete API documentation of the MICO Platform API in the api/c++/doc directory, run

    make doxygen-doc

To install the C++ libraries and headers to the predefined prefix, run

    make install
   
In this case, you would probably also want to make sure that the Hadoop Native Libraries are properly 
installed in the system before.


