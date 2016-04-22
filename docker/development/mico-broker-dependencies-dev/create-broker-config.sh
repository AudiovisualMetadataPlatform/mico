#!/bin/bash
MICO_USER=$(echo "GET mico-base/username" |debconf-communicate |cut -d " " -f2-)
MICO_PASS=$(echo "GET mico-base/password" |debconf-communicate |cut -d " " -f2-)
MICO_HOST="mico-broker"

mkdir -p /usr/share/mico
cat > /etc/tomcat7/Catalina/localhost/broker.xml <<EOF
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
<Context docBase="/usr/share/mico/broker.war" unpackWAR="false" useNaming="true">
    <Parameter name="mico.host" value="$MICO_HOST" override="true"/>
    <Parameter name="mico.user" value="$MICO_USER" override="true"/>
    <Parameter name="mico.pass" value="$MICO_PASS" override="true"/>
    <Parameter name="mico.marmottaBaseUri" value="http://mico-marmotta:8080/marmotta" override="true"/>
    <Parameter name="mico.storageBaseUri" value="file:///data/" override="true"/>
</Context>
EOF
