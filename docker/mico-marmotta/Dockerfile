FROM mico/mico-base
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get -y install postgresql sudo && \
    service postgresql start && (apt-get -y install mico-marmotta; service tomcat7 stop; service postgresql stop) && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY run-tomcat7.sh run-mico-marmotta.sh /

EXPOSE 8080

ENTRYPOINT ["/run-mico-marmotta.sh"]
