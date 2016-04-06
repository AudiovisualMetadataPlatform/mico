#!/bin/bash

function shutdown {
	/usr/bin/killall -u tomcat7
	/usr/sbin/service rabbitmq-server stop
}

trap shutdown EXIT


/usr/sbin/service rabbitmq-server start
if [ "$?" -eq "0" ]; then
	./run-tomcat7.sh
fi
