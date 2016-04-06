#MICO Platform Docker containers

##mico/base
This is just a plain Debian Jessie with MICO repository enabled.

##mico/mico-base
Extends from mico/base with mico-base package installed and password set to `mico`

##mico/mico-marmotta
Extends from mico/mico-base and provides the MICO Marmotta package including PostgreSQL as backend.

##mico/mico-broker
Extends from mico/mico-base and provides the MICO broker including RabbitMQ. Use the `--add-host`, otherwise it will fail:
`docker run -it -p 8080 -p 15672 --add-host mico-broker:127.0.0.1 mico/mico-broker`
