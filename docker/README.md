#MICO Platform private Docker registry
1. Request an account
2. Run `docker login docker.mico-project.eu` and input credentials
3. Go ahead pulling images (see https://docker.mico-project.eu/v2/_catalog for a developer readable list)

##Docker image tags
* *latest*: Latest release
* *dev*: Development (snapshots)

#MICO Platform Docker containers

##docker.mico-project.eu/mico/base
This is just a plain Debian Jessie with MICO repository enabled.

Available tags:

* *latest*
* *dev*: Uses dev repository for Debian packages

##docker.mico-project.eu/mico/mico-base
Extends from mico/base with mico-base package installed and password set to `mico`

Available tags:

* *latest*
* *dev*
* *1.2.10*

##docker.mico-project.eu/mico/mico-marmotta-dependencies
Extends from mico/mico-base and provides all dependencies (e.g. PostgreSQL) for Marmotta including configuration. Useful for development to run a local compiled Marmotta WAR.

Available tags:

* *latest*
* *dev*

Create image and copy Marmotta WAR:

1. `docker create docker.mico-project.eu/mico/mico-marmotta-dependencies`

2. `docker cp /path/to/war/file.war container_name:/usr/share/marmotta/marmotta-webapp.war`

3. `docker start -a container_name`

**OR** create image mounting local directory within image, so you don't need to copy the file every time
`docker run -v /path/to/war/:/usr/share/marmotta/ -t docker.mico-project.eu/mico/mico-marmotta-dependencies` (make sure the WAR is called *marmotta-webapp.war*)

##docker.mico-project.eu/mico/mico-marmotta
Extends from mico/mico-marmotta-dependencies and provides the MICO Marmotta package including PostgreSQL as backend.

Available tags:

* *latest*
* *dev*
* *1.2.10*

`docker run -t docker.mico-project.eu/mico/mico-marmotta`

##docker.mico-project.eu/mico/mico-broker-dependencies
Extends from mico/mico-base and provides all dependencies (e.g. RabbitMQ) for the broker including configuration. Useful for development to run a local compiled broker WAR.

Available tags:

* *latest*
* *dev*

Create image and copy broker WAR:

1. `docker create --add-host mico-broker:127.0.0.1 -p 5672:5672 -p 8080:8080 -p 15672:15672 docker.mico-project.eu/mico/mico-broker-dependencies`

2. `docker cp /path/to/war/file.war container_name:/usr/share/mico/broker.war` This step can be skipped if you just want to use RabbitMQ. Just ignore the Tomcat errors, RabbitMQ is still working.

3. `docker start -a container_name`

**OR** create image mounting local directory within image, so you don't need to copy the file every time
`docker run -v /path/to/war/:/usr/share/mico/ --add-host mico-broker:127.0.0.1 -p 5672:5672 -p 8080:8080 -p 15672:15672 -t docker.mico-project.eu/mico/mico-broker-dependencies` (make sure the WAR is called *broker.war*)

##docker.mico-project.eu/mico/mico-broker
Extends from mico/mico-broker-dependencies and provides the MICO broker including RabbitMQ. Use the `--add-host`, otherwise it will fail:

`docker run -it -p 8080 -p 15672 --add-host mico-broker:127.0.0.1 mico/mico-broker`

Available tags:

* *latest*
* *dev*
* *1.2.10*

##docker.mico-project.eu/mico/mico-persistence-hdfs
Runs an HDFS server (version 2.7.2) on `localhost` (not accessible from remote hosts)

Available tags:

* *latest*

`docker run -it -p 8020:8020 -p 50010:50010 -p 50070:50070 -p 50075:50075 -p 50090:50090 docker.mico-project.eu/mico/mico-persistence-hdfs`

To access the HDFS server from remote systems, add options `-h hdfs_host_name` and `-e HDFS_USE_HOSTNAME=true` to `docker run` and make sure **all** hosts accessing the HDFS server have a proper name resolution of *your_host_name* (e.g. by adding this to `/etc/hosts`).

##docker.mico-project.eu/mico/mico-persistence-ftp
Runs a FTP server.

Available tags:

* *latest*

`docker run -it -p 20:20 -p 21:21 docker.mico-project.eu/mico/mico-persistence-ftp`
