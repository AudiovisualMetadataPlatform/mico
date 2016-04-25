#MICO Platform private Docker registry
1. Request an account
2. Run `docker login docker.mico-project.eu` and input credentials
3. Go ahead pulling images (see https://docker.mico-project.eu/v2/_catalog for a developer readable list)

##Docker image tags
*ToDo: explain (latest, ...)*

#MICO Platform Docker containers

##docker.mico-project.eu/mico/base
This is just a plain Debian Jessie with MICO repository enabled.

##docker.mico-project.eu/mico/mico-base
Extends from mico/base with mico-base package installed and password set to `mico`

##docker.mico-project.eu/mico/mico-marmotta-dependencies
Extends from mico/mico-base and provides all dependencies (e.g. PostgreSQL) for Marmotta including configuration. Useful for development to run a local compiled Marmotta WAR.
Create image and copy Marmotta WAR:

1. `docker create docker.mico-project.eu/mico/mico-marmotta-dependencies`

2. `docker cp /path/to/war/file.war container_name:/usr/share/marmotta/marmotta-webapp.war`

3. `docker start -a container_name`

**OR** create image mounting local directory within image, so you don't need to copy the file every time
`docker run -v /path/to/war/:/usr/share/marmotta/ -t docker.mico-project.eu/mico/mico-marmotta-dependencies` (make sure the WAR is called *marmotta-webapp.war*)

##docker.mico-project.eu/mico/mico-marmotta
Extends from mico/mico-marmotta-dependencies and provides the MICO Marmotta package including PostgreSQL as backend.

`docker run -t docker.mico-project.eu/mico/mico-marmotta`

##docker.mico-project.eu/mico/mico-broker-dependencies
Extends from mico/mico-base and provides all dependencies (e.g. RabbitMQ) for the broker including configuration. Useful for development to run a local compiled broker WAR.
Create image and copy broker WAR:

1. `docker create --add-host mico-broker:127.0.0.1 docker.mico-project.eu/mico/mico-broker-dependencies`

2. `docker cp /path/to/war/file.war container_name:/usr/share/mico/broker.war`

3. `docker start -a container_name`

**OR** create image mounting local directory within image, so you don't need to copy the file every time
`docker run -v /path/to/war/:/usr/share/mico/ --add-host mico-broker:127.0.0.1 -t docker.mico-project.eu/mico/mico-broker-dependencies` (make sure the WAR is called *broker.war*)

##docker.mico-project.eu/mico/mico-broker
Extends from mico/mico-broker-dependencies and provides the MICO broker including RabbitMQ. Use the `--add-host`, otherwise it will fail:

`docker run -it -p 8080 -p 15672 --add-host mico-broker:127.0.0.1 mico/mico-broker`