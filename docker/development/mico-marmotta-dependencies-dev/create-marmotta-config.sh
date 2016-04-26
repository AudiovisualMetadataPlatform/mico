#!/bin/bash
set -e

MICO_USER=$(echo "GET mico-base/username" |debconf-communicate |cut -d " " -f2-)
MICO_PASS=$(echo "GET mico-base/password" |debconf-communicate |cut -d " " -f2-)
MICO_HOST="mico-marmotta"
MARMOTTA_DB_NAME="marmotta"
MARMOTTA_HOME="/var/lib/marmotta"

#Setup DB
/usr/sbin/service postgresql start
/usr/bin/sudo -u postgres /usr/bin/psql postgres -c "CREATE USER $MICO_USER PASSWORD '$MICO_PASS'" >/dev/null 2>&1
/usr/bin/sudo -u postgres /usr/bin/psql postgres -c "CREATE DATABASE $MARMOTTA_DB_NAME OWNER $MICO_USER" >/dev/null 2>&1
/usr/sbin/service postgresql stop


#Setup Marmotta
mkdir -p $MARMOTTA_HOME

cat >$MARMOTTA_HOME/system-config.properties <<EOF
database.type = postgres
database.url = jdbc:postgresql://localhost:5432/$MARMOTTA_DB_NAME?prepareThreshold=3
database.user = $MICO_USER
database.password = $MICO_PASS
#kiwi.host = http://$MICO_HOST:8080/marmotta/
#kiwi.context = http://$MICO_HOST:8080/marmotta/
#kiwi.setup.host = true
marmotta.home = $MARMOTTA_HOME
EOF

chown tomcat7:tomcat7 -R $MARMOTTA_HOME

#Config Tomcat
mkdir -p /usr/share/marmotta
cat > /etc/tomcat7/Catalina/localhost/marmotta.xml <<EOF
<!--

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<Context docBase="/usr/share/marmotta/marmotta-webapp.war" unpackWAR="false" useNaming="true">
  <Parameter name="marmotta.home" value="/var/lib/marmotta" override="false" />
</Context>
EOF
