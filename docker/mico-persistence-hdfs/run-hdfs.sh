#!/bin/bash

function startup {
	service ssh start && \
	echo exit |sudo -u hadoop ssh -o StrictHostKeyChecking=no $HOSTNAME && \
	sudo -u hadoop /opt/hadoop/sbin/start-dfs.sh && \
	((sleep 5 && echo -e "\n------------------------------------------------\nWait for image sync (safemode) to be finished...") &)
}


function shutdown {
	echo Shutting down...
	sudo -u hadoop /opt/hadoop/sbin/stop-dfs.sh
	service ssh stop
	exit 0
}


trap shutdown EXIT

startup
tail -f /var/log/hadoop/*.log
shutdown
