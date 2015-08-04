add_library(mico_logging SHARED Logging.cpp)

target_link_libraries(mico_logging ${CMAKE_THREAD_LIBS_INIT})

set_target_properties(mico_logging PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")

install (TARGETS mico_logging DESTINATION lib)
install (FILES Logging.hpp DESTINATION include)
