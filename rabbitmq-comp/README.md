# Mico camel component

this camel component triggerst mico extractors through rabbitmq

## available options
* host
* user
* password
*

## usage

java dsl
 `.to("mico-comp:vbox?host=[ip-of-mico-platform]&user=[userName]&password=[secret]");`

