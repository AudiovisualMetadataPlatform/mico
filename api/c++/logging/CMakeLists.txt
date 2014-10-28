add_library(mico_logging SHARED Logging.cpp)

target_link_libraries(mico_logging ${CMAKE_THREAD_LIBS_INIT})

install (FILES Logging.hpp DESTINATION include)
