FROM mico/mico-broker-dependencies
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Fetch package information
RUN apt-get update

#Install mico-broker from package
##To avoid installing mico-marmotta and mico-persistence as mico-broker dependencies
##the broker is just unpacked and Tomcat gets a manual configuration.
RUN apt-get download mico-broker && \
    dpkg --ignore-depends=mico-marmotta,mico-persistence --unpack mico-broker_*.deb && \
    rm mico-broker_*.deb

#Cleanup
RUN apt-get clean && rm -rf /var/lib/apt/lists/*



