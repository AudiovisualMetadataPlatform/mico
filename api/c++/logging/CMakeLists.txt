add_library(mico_logging SHARED Logging.cpp)

target_link_libraries(mico_logging ${CMAKE_THREAD_LIBS_INIT})

set_target_properties(mico_logging PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")
if (CMAKE_BUILD_TYPE MATCHES "Debug")
    set_target_properties(mico_logging PROPERTIES COMPILE_DEFINITIONS "DEBUG_BUILD")
endif(CMAKE_BUILD_TYPE MATCHES "Debug")

install (TARGETS mico_logging DESTINATION lib)
install (FILES Logging.hpp DESTINATION include)
