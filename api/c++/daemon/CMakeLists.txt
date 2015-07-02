include_directories(../event ../rdf ../persistence ../logging)

add_library(mico_daemon SHARED Daemon.cpp)

target_link_libraries(mico_daemon mico_event ${CMAKE_THREAD_LIBS_INIT} ${LIBDAEMON_LIBRARY} ${AMQPCPP_LIBRARIES})

set_target_properties(mico_daemon PROPERTIES SOVERSION "${MICO_PLATFORM_VERSION}")

install (TARGETS mico_daemon DESTINATION lib)
install (FILES Daemon.hpp DESTINATION include)
