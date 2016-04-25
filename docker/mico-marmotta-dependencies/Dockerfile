#This is the base for a Marmotta image, including all dependencies and configuration files.
#Only Marmotta itself (/usr/share/marmotta/marmotta-webapp.war) is missing.

FROM mico/mico-base
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Install mico-marmotta dependencies
RUN apt-get update && \
    apt-get -y install `apt-cache depends mico-marmotta | awk '/[^\|]Depends:/{print$2}'` && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

#Config Marmotta
COPY create-marmotta-config.sh /
RUN /create-marmotta-config.sh && rm /create-marmotta-config.sh

#Provide scripts to run PostgreSQL and Tomcat
COPY run-tomcat7.sh run-mico-marmotta.sh /

#Tomcat port
EXPOSE 8080
ENTRYPOINT ["/run-mico-marmotta.sh"]
