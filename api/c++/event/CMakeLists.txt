include_directories(../rdf ../amqp ../persistence)

add_library(mico_event Event.pb.cc EventManager.cpp)
