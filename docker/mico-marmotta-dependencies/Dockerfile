#This is the base for a marmotta image, including all dependencies.
#Marmotta itself and the proper Tomcat configuration is not part of this image.

FROM mico/mico-base
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Install mico-marmotta dependencies
RUN apt-get update && \
    apt-get -y install `apt-cache depends mico-marmotta | awk '/[^\|]Depends:/{print$2}'` && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


#Provide scripts to run PostgreSQL and Tomcat
COPY run-tomcat7.sh run-mico-marmotta.sh /

#Tomcat port
EXPOSE 8080
ENTRYPOINT ["/run-mico-marmotta.sh"]
