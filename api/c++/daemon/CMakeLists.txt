include_directories(../event ${CMAKE_CURRENT_BINARY_DIR}/../event/protobuf_out ../rdf ../persistence ../logging)

link_directories("${CMAKE_CURRENT_BINARY_DIR}/../../../anno4cpp/")

add_library(mico_daemon SHARED Daemon.cpp)

target_link_libraries(mico_daemon mico_event ${CMAKE_THREAD_LIBS_INIT} ${LIBDAEMON_LIBRARY})

set_target_properties(mico_daemon PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")

install (TARGETS mico_daemon DESTINATION lib)
install (FILES Daemon.hpp DESTINATION include)
