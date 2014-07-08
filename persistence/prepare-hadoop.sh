#!/bin/bash

VERSION=$1
DIR="target/hadoop-$VERSION"

if [ ! -d "$DIR" ] && [ ! -d "$DIR-src" ]; 
then
    echo "ERROR: hadoop distributions not found at $DIR*"
    exit -1
fi

(cd $DIR-src; mvn package -Pnative -DskipTests)
(cd $DIR/lib/native/; mkdir dist; mv *.* dist)
cp $DIR-src/hadoop-tools/hadoop-pipes/target/native/*.a $DIR/lib/native/
cp $DIR-src/hadoop-common-project/hadoop-common/target/native/target/usr/local/lib/libhadoop.so.1.0.0 $DIR/lib/native/libhadoop.so
cp $DIR-src/hadoop-common-project/hadoop-common/target/native/target/usr/local/lib/libhadoop.a $DIR/lib/native/
cp $DIR-src/hadoop-hdfs-project/hadoop-hdfs/target/native/target/usr/local/lib/libhdfs.so.0.0.0 $DIR/lib/native/libhdfs.so
cp $DIR-src/hadoop-hdfs-project/hadoop-hdfs/target/native/target/usr/local/lib/libhdfs.a $DIR/lib/native/

