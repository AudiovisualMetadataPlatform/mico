cmake_minimum_required(VERSION 2.8.4)
project(mico-platform)

if (NOT DEFINED MICO_PLATFORM_VERSION)
    set(MICO_PLATFORM_VERSION "2.0.0")
endif()

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

include_directories(${AMQPCPP_INCLUDE_DIRS})


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
