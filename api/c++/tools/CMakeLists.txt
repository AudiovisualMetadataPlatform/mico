include_directories(../rdf ../persistence ../event ${CMAKE_CURRENT_BINARY_DIR}/../event/protobuf_out ../logging)

link_directories("${CMAKE_CURRENT_BINARY_DIR}/../../../anno4cpp/")

add_executable(mico_inject mico_inject.cpp)
target_link_libraries(mico_inject mico_event mico_logging ${LibMagic_LIBRARY} ${CMAKE_THREAD_LIBS_INIT} ${AMQPCPP_LIBRARIES})

install(TARGETS mico_inject RUNTIME DESTINATION bin)
