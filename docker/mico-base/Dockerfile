FROM mico/base

MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get -y install mico-base && \
    echo "set mico-base/password mico" | debconf-communicate && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
