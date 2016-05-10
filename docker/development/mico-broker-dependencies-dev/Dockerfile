#This is the base for a broker image, including all dependencies and configuration files.
#Only the broker itself (/usr/share/mico/broker.war) is missing.

FROM mico/mico-base:dev
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Fetch package information
RUN apt-get update

#Install RabbitMQ (mico-rabbitmq) in separate step, as MICO specific config is required
RUN apt-get -y install rabbitmq-server && \
    echo "NODENAME=\"rabbit@localhost\"" > /etc/rabbitmq/rabbitmq-env.conf && \
    service rabbitmq-server start && \
    apt-get -y install mico-rabbitmq && \
    service rabbitmq-server stop

#Install dependencies of mico-broker, excluding mico-marmotta and mico-persistence
RUN apt-get -t jessie-backports -y install openjdk-8-jre-headless
RUN apt-get -y install `apt-cache depends mico-broker | awk '/[^\|]Depends:/{print$2}' |grep -v \< |grep -v mico-marmotta | grep -v mico-persistence`

#Config Tomcat
COPY create-broker-config.sh /
RUN mkdir /data && chmod 0777 /data && \
    /create-broker-config.sh && \
    rm /create-broker-config.sh

#Provide scripts to run Tomcat and RabbitMQ
RUN apt-get -y install sudo
COPY run-tomcat7.sh run-mico-broker.sh /

#Cleanup
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

#Tomcat and RabbitMQ port
EXPOSE 8080 5672
ENTRYPOINT ["/run-mico-broker.sh"]
