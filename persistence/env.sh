#!/bin/bash

if [ $# -eq 0 ]
then
    VERSION="2.4.1"
else
    VERSION=$1
fi

export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")

HADOOP_HOME="target/hadoop-$VERSION"
export HADOOP_HOME

export PATH=$HADOOP_HOME/bin:$PATH

