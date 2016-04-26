FROM mico/mico-marmotta-dependencies:dev
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Install mico-marmotta
RUN apt-get update && \
    service postgresql start && (apt-get -y install mico-marmotta; service tomcat7 stop; service postgresql stop) && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

