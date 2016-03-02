#!/bin/bash
scp target/broker.war user@mico-box:/home/user/

ssh user@mico-box <<-'ENDSSH'
sudo -S service tomcat7 stop
user
echo
echo 'wait 5 seconds for tomcat to stop'
sleep 5
sudo cp ~/broker.war /usr/share/mico/broker.war
echo copied broker.war, start tomcat ...
sudo service tomcat7 start
ENDSSH

echo 'update finished'