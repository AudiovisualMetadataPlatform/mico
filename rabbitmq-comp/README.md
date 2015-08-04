# Mico camel component

This camel component triggerst mico extractors by sending Analyse events through RabbitMQ.
It tries to read the URIs of content item and part from the message headers, 
if there are no suitable header entries it tries to get the URIs from the content itself.

## available options
* `host` - ip adress or dns name of mico host system (default: mico-platform)
* `user` - user name to access mico-platform
* `password` - to authorize user against mico-platform
* `rabbitPort` - the port of the rabbitMQ broker (default: 5672)
* `serviceID` - the id/uri of extractor which should be triggered for a given content part

## usage

java dsl
 `.to("mico-comp:[NAME]?host=[ip-of-mico-platform]&user=[userName]&password=[secret]");`

