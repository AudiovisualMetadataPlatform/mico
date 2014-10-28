add_library(mico_logging SHARED Logging.cpp)

target_link_libraries(mico_logging ${CMAKE_THREAD_LIBS_INIT})

set_target_properties(mico_logging PROPERTIES SOVERSION 1)

install (TARGETS mico_logging DESTINATION lib)
install (FILES Logging.hpp DESTINATION include)
