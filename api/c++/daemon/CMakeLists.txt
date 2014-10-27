include_directories(../event ../rdf ../persistence ../amqp)

add_library(mico_daemon SHARED Daemon.cpp)

target_link_libraries(mico_daemon mico_event ${CMAKE_THREAD_LIBS_INIT} ${LIBDAEMON_LIBRARY} )

set_target_properties(mico_daemon PROPERTIES SOVERSION 1)

install (TARGETS mico_daemon DESTINATION lib)
install (FILES Daemon.hpp DESTINATION include)
