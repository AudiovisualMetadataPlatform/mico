#!/bin/bash

function shutdown {
	/usr/bin/killall -u tomcat7
	/usr/sbin/service postgresql stop
}

trap shutdown EXIT


/usr/sbin/service postgresql start
if [ "$?" -eq "0" ]; then
	./run-tomcat7.sh
fi
