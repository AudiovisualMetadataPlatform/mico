cmake_minimum_required(VERSION 2.8.4)
project(mico-platform)

ADD_DEFINITIONS(
    -std=c++11 # Or -std=c++0x
    # Other flags
)

add_subdirectory(amqp)
add_subdirectory(rdf)
add_subdirectory(persistence)
add_subdirectory(event)
add_subdirectory(samples)
#add_subdirectory(test)
add_subdirectory(tools)

