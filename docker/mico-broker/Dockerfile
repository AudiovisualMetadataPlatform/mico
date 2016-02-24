FROM mico/mico-base
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

#Fetch package information
RUN apt-get update

#Install Tomcat
RUN apt-get -y install tomcat7 openjdk-7-jre-headless sudo

#Install RabbitMQ (mico-rabbitmq)
RUN apt-get -y install rabbitmq-server && \
    echo "NODENAME=\"rabbit@localhost\"" > /etc/rabbitmq/rabbitmq-env.conf && \
    service rabbitmq-server start && \
    apt-get -y install mico-rabbitmq && \
    service rabbitmq-server stop

#Install mico-broker
##To avoid installing mico-marmotta and mico-persistence as mico-broker dependencies
##the broker is just unpacked and Tomcat gets a manual configuration.
COPY create-broker-config.sh /
RUN apt-get download mico-broker && \
    dpkg --ignore-depends=mico-marmotta,mico-persistence --unpack mico-broker_*.deb && \
    rm mico-broker_*.deb && \
    mkdir /data && chmod 0777 /data && \
    /create-broker-config.sh && \
    rm /create-broker-config.sh

#Cleanup
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

COPY run-tomcat7.sh run-mico-broker.sh /
EXPOSE 8080 5672
ENTRYPOINT ["/run-mico-broker.sh"]
