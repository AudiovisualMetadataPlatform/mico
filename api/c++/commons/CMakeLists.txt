cmake_minimum_required(VERSION 2.8.12)

project("Mico platform commons")

set(TARGET_NAME micoapicommons)

if (UNIX)
  add_definitions(-std=c++11)
endif()

find_package( Boost REQUIRED COMPONENTS system log log_setup filesystem regex)

include_directories(
  .
  ${Boost_INCLUDE_DIRS}
)

add_library(${TARGET_NAME} SHARED
  TimeInfo.h
  FileOperations.h
  FileOperations.cpp
)

target_link_libraries(${TARGET_NAME}
  ${Boost_LIBRARIES}
)

set_target_properties(micoapicommons PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")

install (TARGETS micoapicommons DESTINATION lib)
install (FILES TimeInfo.h FileOperations.h DESTINATION include)
