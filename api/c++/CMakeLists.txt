cmake_minimum_required(VERSION 2.8.4)
project(mico-platform)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/modules/")

ADD_DEFINITIONS(
    -std=c++11 # Or -std=c++0x
    # Other flags
    -DBOOST_LOG_DYN_LINK
)

find_package (Threads)
find_package(CURL)
find_package(EXPAT)
find_package(Protobuf)
find_package(Boost COMPONENTS system log log_setup REQUIRED)
find_package(TESSERACT)
find_package(LibMagic)
find_package(Doxygen)
option(BUILD_DOCUMENTATION "Create and install the HTML based API documentation (requires Doxygen)" ${DOXYGEN_FOUND})

if(BUILD_DOCUMENTATION)
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

    install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/doc/html DESTINATION share/doc)
endif()


add_subdirectory(amqp)
add_subdirectory(rdf)
add_subdirectory(persistence)
add_subdirectory(event)
add_subdirectory(samples)
add_subdirectory(test)
add_subdirectory(tools)

