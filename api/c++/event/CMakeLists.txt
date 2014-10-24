include_directories(../rdf ../amqp ../persistence ${PROTOBUF_INCLUDE_DIR})

add_library(mico_event Event.pb.cc EventManager.cpp)
target_link_libraries(mico_event ${PROTOBUF_LIBRARIES} ${Boost_LOG_LIBRARY} ${Boost_SYSTEM_LIBRARY})

install (TARGETS mico_event DESTINATION lib)
install (FILES EventManager.hpp DESTINATION include)