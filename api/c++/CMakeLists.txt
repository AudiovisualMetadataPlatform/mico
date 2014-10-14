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

add_subdirectory(amqp)
add_subdirectory(rdf)
add_subdirectory(persistence)
add_subdirectory(event)
add_subdirectory(samples)
add_subdirectory(test)
add_subdirectory(tools)

