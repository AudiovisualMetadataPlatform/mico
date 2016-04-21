FROM mico/mico-base
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Fetch package information
RUN apt-get update && \
    apt-get -y install mico-persistence && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY run-proftpd.sh /

#FTP ports
EXPOSE 20 21

ENTRYPOINT ["/run-proftpd.sh"]
