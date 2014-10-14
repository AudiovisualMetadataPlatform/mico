include_directories(../rdf ../amqp ../persistence)

add_library(mico_event Event.pb.cc EventManager.cpp)

install (TARGETS mico_event DESTINATION lib)
install (FILES EventManager.hpp DESTINATION include)