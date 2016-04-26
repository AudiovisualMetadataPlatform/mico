FROM debian:8
MAINTAINER Horst Stadler "horst.stadler@salzburgresearch.at"

ENV DEBIAN_FRONTEND noninteractive

ADD jessie-backports.list /etc/apt/sources.list.d/jessie-backports.list
ADD mico.list /etc/apt/sources.list.d/mico.list
ADD mico-repository.key /root/mico-repository.key
RUN apt-key add /root/mico-repository.key && rm /root/mico-repository.key
RUN apt-get update && apt-get -y upgrade && apt-get clean && rm -rf /var/lib/apt/lists/*
