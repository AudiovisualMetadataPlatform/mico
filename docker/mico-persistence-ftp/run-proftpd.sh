#!/bin/bash

OPTIONS=""
CONFIG_FILE=/etc/proftpd/proftpd.conf

[ -r /etc/default/proftpd ] && . /etc/default/proftpd

/usr/sbin/proftpd -n -c $CONFIG_FILE $OPTIONS
