include_directories(../amqp ../rdf ../persistence ../event)

add_executable(mico_inject mico_inject.cpp)
target_link_libraries(mico_inject mico_event ${LibMagic_LIBRARY})

install(TARGETS mico_inject RUNTIME DESTINATION bin)