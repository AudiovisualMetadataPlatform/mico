include_directories(../amqp ../rdf ../persistence ../event ../logging)

add_executable(mico_inject mico_inject.cpp)
target_link_libraries(mico_inject mico_event mico_logging ${LibMagic_LIBRARY} ${CMAKE_THREAD_LIBS_INIT})

install(TARGETS mico_inject RUNTIME DESTINATION bin)