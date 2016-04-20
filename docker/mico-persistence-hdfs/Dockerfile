FROM debian:8
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install default-jre-headless ssh sudo curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN curl -SL --insecure https://www.eu.apache.org/dist/hadoop/common/stable/hadoop-2.7.2.tar.gz |tar -zxC /opt && \
    ln -s /opt/hadoop-2.7.2 /opt/hadoop && \
    ln -s /opt/hadoop/etc/hadoop/ /etc/hadoop

RUN mkdir -p /var/lib/hadoop/datanode /var/lib/hadoop/namenode /var/lib/hadoop/home /var/log/hadoop && \
    useradd -d /var/lib/hadoop/home -K UID_MIN=100 -K UID_MAX=500 -K GID_MIN=100 -K GID_MAX=500 -K PASS_MAX_DAYS=-1 -U hadoop && \
    chown hadoop:hadoop /var/lib/hadoop/* /var/log/hadoop && \
    chmod 700 /var/lib/hadoop/home && \
    sudo -u hadoop -- ssh-keygen -t dsa -P '' -f /var/lib/hadoop/home/.ssh/id_dsa && \
    sudo -u hadoop cp /var/lib/hadoop/home/.ssh/id_dsa.pub /var/lib/hadoop/home/.ssh/authorized_keys && \
    service ssh start && echo exit |sudo -u hadoop ssh -o StrictHostKeyChecking=no localhost && echo exit |sudo -u hadoop ssh -o StrictHostKeyChecking=no 0.0.0.0 && service ssh stop

COPY core-site.xml hdfs-site.xml /etc/hadoop/
RUN sed -i 's/\${JAVA_HOME}/\/usr\/lib\/jvm\/default-java\/jre/g' /etc/hadoop/hadoop-env.sh && \
    sed -i 's/#export HADOOP_LOG_DIR=\${HADOOP_LOG_DIR}\/\$USER/export HADOOP_LOG_DIR=\/var\/log\/hadoop/g' /etc/hadoop/hadoop-env.sh
COPY hdfs /etc/init.d/

RUN sudo -u hadoop /opt/hadoop/bin/hdfs namenode -format && \
    service ssh start && sudo -u hadoop /opt/hadoop/sbin/start-dfs.sh && echo "Waiting for HDFS to leave safemode...." && sleep 15 && \
    sudo -u hadoop /opt/hadoop/bin/hdfs dfs -chmod 777 / && \
    sudo -u hadoop /opt/hadoop/bin/hdfs dfs -setrep -R -w 1 / && \
    sudo -u hadoop /opt/hadoop/sbin/stop-dfs.sh && service ssh stop && \
    rm /var/log/hadoop/*

COPY run-hdfs.sh /
EXPOSE 8020 50010 50070 50075 50090
ENTRYPOINT ["/run-hdfs.sh"]
