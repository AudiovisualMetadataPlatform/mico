#!/bin/bash

function startup {
	HDFS_HOSTNAME=localhost	
	if [ "$HDFS_USE_HOSTNAME" == "true" ]; then
		HDFS_HOSTNAME=$HOSTNAME
	fi

	echo -e "<!-- DO NOT CHANGE! File gets overwritten on startup. -->\n\n" | tee /etc/hadoop/core-site.xml /etc/hadoop/hdfs-site.xml >/dev/null
	echo "#DO NOT CHANGE! File gets overwritten on startup" > /etc/hadoop/slaves
	sed "s/%HDFS_HOSTNAME%/${HDFS_HOSTNAME}/g" /etc/hadoop/core-site.xml.tmpl >> /etc/hadoop/core-site.xml
	sed "s/%HDFS_HOSTNAME%/${HDFS_HOSTNAME}/g" /etc/hadoop/hdfs-site.xml.tmpl >> /etc/hadoop/hdfs-site.xml
	sed "s/%HDFS_HOSTNAME%/${HDFS_HOSTNAME}/g" /etc/hadoop/slaves.tmpl >> /etc/hadoop/slaves

	service ssh start && \
	echo exit |sudo -u hadoop ssh -o StrictHostKeyChecking=no $HOSTNAME && \
	sudo -u hadoop /opt/hadoop/sbin/start-dfs.sh && \
	((sleep 5 && echo -e "\n------------------------------------------------\nHDFS is available via ${HDFS_HOSTNAME}\nWait for image sync (safemode) to be finished...") &)
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
