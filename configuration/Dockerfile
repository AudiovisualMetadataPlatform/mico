FROM debian:jessie

ENV DEBIAN_FRONTEND noninteractive

EXPOSE 80
EXPOSE 8080
EXPOSE 15672

RUN apt-get update
RUN apt-get install -y software-properties-common
RUN add-apt-repository 'deb http://apt.mico-project.eu/ mico main contrib non-free'
RUN wget -O- http://apt.mico-project.eu/apt-repo.key | sudo apt-key add -
RUN apt-get update
RUN apt-get install mico-platform
