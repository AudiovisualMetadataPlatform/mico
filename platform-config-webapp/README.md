prepare target machine
----------------------

1. install extractors
2. grant permission to tomcat user, to start and stop extractors without password by adding a line like this in sudoers list (`sudo visudo`)

        tomcat7 ALL=(ALL:ALL) NOPASSWD: /home/user/Downloads/extractors-public/configurations/mico-config-extractors.sh

3. store webapp war in `/usr/share/mico/mico-conf.war`
4. store properties file with available extractor configurations in `/usr/share/mico/platform-config.properties` and insert values such as
		[displayName]=[path to config start script]
		animal-detection=/usr/share/mico/configurations/mico-detect-animals.sh

5. Tell tomcat where to find the webapp
  * create `/var/lib/tomcat7/conf/Catalina/localhost/mico-conf.xml` and insert 

			<Context docBase="/usr/share/mico/mico-conf.war" unpackWAR="false" useNaming="true">
      		  <Parameter name="conf.script" value="/home/user/Downloads/extractors-public/configurations/mico-config-extractors.sh"/>
      		  <Parameter name="mico.host" value="mico-platform" override="true"/>
      		  <Parameter name="mico.user" value="mico" override="true"/>
      		  <Parameter name="mico.pass" value="mico" override="true"/>
    		</Context>
  * restart tomcat: sudo service tomcat7 restart