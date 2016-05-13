cmake_minimum_required(VERSION 2.8.4)
project(mico-platform)
get_filename_component(PLATFORM_ROOT "${CMAKE_SOURCE_DIR}/../../" ABSOLUTE)

execute_process(
    COMMAND bash get_mvn_platform_version.txt
    OUTPUT_VARIABLE POM_PLATFORM_VERSION
    ERROR_VARIABLE POM_PLATFORM_VERSION_ERROR
    WORKING_DIRECTORY ${PLATFORM_ROOT}
)
message(STATUS "POM_PLATFORM_VERSION =" ${POM_PLATFORM_VERSION})
string(REGEX MATCH "([0-9]+)(\\.[0-9]+)?(\\.[0-9]+)?" CPP_PLATFORM_VERSION ${POM_PLATFORM_VERSION})
message(STATUS "CPP_PLATFORM_VERSION =" ${CPP_PLATFORM_VERSION})
if (NOT DEFINED MICO_PLATFORM_VERSION)
    set(MICO_PLATFORM_VERSION ${CPP_PLATFORM_VERSION})
else()
    if (NOT ${MICO_PLATFORM_VERSION} MATCHES ${CPP_PLATFORM_VERSION})
        message(FATAL_ERROR "Platform version set in Debian config does not match POM version - Please update Debian config")
    endif()
endif()

message(STATUS "MICO_PLATFORM_VERSION=" ${MICO_PLATFORM_VERSION})

include(ExternalProject)

set(ANNO4CPPDEPS_ROOT ${CMAKE_CURRENT_BINARY_DIR}/install_anno4cpp)

ExternalProject_Add(anno4cpp_ext
    PREFIX externals
    GIT_REPOSITORY git@bitbucket.org:mico-project/anno4cpp.git
    GIT_TAG develop
    CMAKE_ARGS
      -DMICO_PLATFORM_PARENT_VERSION=${POM_PLATFORM_VERSION}
      -DCMAKE_BUILD_TYPE=Debug
      -DCMAKE_INSTALL_PREFIX:PATH=${ANNO4CPPDEPS_ROOT})


string(REPLACE "." ";" VERSION_LIST ${MICO_PLATFORM_VERSION})
list(GET VERSION_LIST 0 MICO_PLATFORM_VERSION_MAJOR)
list(GET VERSION_LIST 1 MICO_PLATFORM_VERSION_MINOR)
list(GET VERSION_LIST 2 MICO_PLATFORM_VERSION_PATCH)

set(MICO_PLATFORM_SOVERSION ${MICO_PLATFORM_VERSION_MAJOR})

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/modules/")

ADD_DEFINITIONS(
    -std=c++11 # Or -std=c++0x
    # Other flags
    -DBOOST_LOG_DYN_LINK
)

set (CMAKE_POSITION_INDEPENDENT_CODE TRUE)

find_package (Threads)
find_package(CURL REQUIRED)
find_package(HDFS)
find_package(EXPAT)
find_package(Protobuf 2.6 REQUIRED)
find_package(Boost COMPONENTS system log REQUIRED)
find_package(TESSERACT)
find_package(LibMagic)
find_package(AMQPCPP REQUIRED)
find_package(Doxygen)
find_package(LibDaemon)
find_package(JNI REQUIRED)

message(STATUS "Using JNI_INCLUDE_DIRS = " ${JNI_INCLUDE_DIRS})
message(STATUS "Using JNI_LIBRARIES = " ${JNI_LIBRARIES})

#this line creates the links file info with the current version for the debian packaging
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/debian/libmico-api1.links.in ${CMAKE_CURRENT_SOURCE_DIR}/debian/libmico-api1.links @ONLY)

option(BUILD_DOCUMENTATION "Create and install the HTML based API documentation (requires Doxygen)" )

if(MICO_PLATFORM_BUILD_DOCUMENTATION AND DOXYGEN_FOUND) 
    if(NOT DOXYGEN_FOUND)
        message(FATAL_ERROR "Doxygen is needed to build the documentation.")
    endif()

    set(doxyfile_in ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in)
    set(doxyfile ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile)

    configure_file(${doxyfile_in} ${doxyfile} @ONLY)

    add_custom_target(doc ALL
        COMMAND ${DOXYGEN_EXECUTABLE} ${doxyfile}
        WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
        COMMENT "Generating API documentation with Doxygen"
        VERBATIM)

    install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/doc/html DESTINATION share/doc/libmico-api1)
endif()

include_directories(
  ${AMQPCPP_INCLUDE_DIRS}
  ${JNI_INCLUDE_DIRS}
  ${ANNO4CPPDEPS_ROOT}/include
)

if (UNIX)
  get_filename_component(JAVA_AWT_PATH ${JAVA_AWT_LIBRARY} DIRECTORY)
  link_directories(${JAVA_AWT_PATH}/xawt/ )
endif()

link_directories(${ANNO4CPPDEPS_ROOT}/lib/)

add_subdirectory(commons)
add_subdirectory(logging)
add_subdirectory(rdf)
add_subdirectory(persistence)
add_subdirectory(event)
add_subdirectory(daemon)
add_subdirectory(samples)
add_subdirectory(tools)

if ((NOT DEFINED MICO_PLATFORM_BUILD_TESTS) OR (${MICO_PLATFORM_BUILD_TESTS}) )
    add_subdirectory(test)
endif()
