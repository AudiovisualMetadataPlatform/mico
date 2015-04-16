include_directories(../rdf ../persistence ../logging ${PROTOBUF_INCLUDE_DIR})

add_library(mico_event SHARED Event.pb.cc EventManager.cpp)
target_link_libraries(mico_event mico_logging mico_rdf mico_persistence ${PROTOBUF_LIBRARIES} ${Boost_SYSTEM_LIBRARY} ${CMAKE_THREAD_LIBS_INIT} ${AMQPCPP_LIBRARIES})

set_target_properties(mico_event PROPERTIES SOVERSION 1)

install (TARGETS mico_event DESTINATION lib)
install (FILES AnalysisService.hpp EventManager.hpp DESTINATION include)
