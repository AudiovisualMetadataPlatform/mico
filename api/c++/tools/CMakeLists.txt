include_directories(../rdf ../persistence ../event)

add_executable(mico_inject mico_inject.cpp)
target_link_libraries(mico_inject mico_marmotta mico_rdf mico_persistence mico_event mico_amqp)