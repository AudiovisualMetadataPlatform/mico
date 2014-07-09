#!/bin/bash

source env.sh

if [ -n "${HADOOP_HOME+1}" ]; then
    echo "Using Hadoop from $HADOOP_HOME ..."
else
    echo "HADOOP_HOME is not defined"
    exit -1
fi

# Format the namenode
hdfs namenode -format

# Start the namenode
hdfs namenode

# Start a datanode is not required in this environment
#hdfs datanode

