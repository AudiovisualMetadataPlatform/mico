include_directories(../rdf ../amqp ../persistence ${PROTOBUF_INCLUDE_DIR})

add_library(mico_event SHARED Event.pb.cc EventManager.cpp)
target_link_libraries(mico_event mico_amqp mico_rdf mico_persistence ${PROTOBUF_LIBRARIES} ${Boost_LOG_LIBRARY} ${Boost_SYSTEM_LIBRARY})

set_target_properties(mico_event PROPERTIES SOVERSION 1)

install (TARGETS mico_event DESTINATION lib)
install (FILES EventManager.hpp DESTINATION include)