find_package(Protobuf)

include_directories(
  ../rdf
  ../persistence
  ../event
  ${CMAKE_CURRENT_BINARY_DIR}/../event/protobuf_out
)

add_library(gtest STATIC gtest-all.cc)

add_executable(model_tests rdf_tests.cpp uri_tests.cpp bnode_tests.cpp literal_tests.cpp)
target_link_libraries(model_tests gtest mico_rdf)

add_executable(query_tests rdf_tests.cpp sparql_parse_tests.cpp)
target_link_libraries(query_tests gtest mico_rdf)

add_executable(client_tests rdf_tests.cpp http_request_tests.cpp http_client_tests.cpp )
target_link_libraries(client_tests gtest mico_rdf mico_marmotta curl boost_system boost_log boost_log_setup)

#add_executable(sparql_tests rdf_tests.cpp sparql_client_tests.cpp )
#target_link_libraries(sparql_tests gtest mico_rdf mico_marmotta curl boost_system boost_log boost_log_setup)

add_executable(persistence_service_tests
    # persistence_tests.cpp
    persistence_service_tests.cpp
)

set_target_properties(persistence_service_tests PROPERTIES COMPILE_DEFINITIONS "JNIPP_USE_TYPE_TRAITS")
set_target_properties(persistence_service_tests PROPERTIES COMPILE_OPTIONS "-O3;-std=c++11;-ffunction-sections;-fdata-sections")

target_link_libraries(persistence_service_tests gtest mico_rdf mico_marmotta mico_persistence curl boost_system boost_log boost_log_setup)

add_executable(metadata_tests  persistence_tests.cpp metadata_tests.cpp )
target_link_libraries(metadata_tests gtest mico_rdf mico_marmotta mico_persistence curl boost_system boost_log boost_log_setup)

#add_executable(content_item_tests  persistence_tests.cpp content_item_tests.cpp )
#target_link_libraries(content_item_tests gtest mico_rdf mico_marmotta mico_persistence curl boost_system boost_log boost_log_setup)

add_executable(urlstream_tests  persistence_tests.cpp urlstream_tests.cpp )
target_link_libraries(urlstream_tests gtest mico_rdf mico_marmotta mico_persistence curl boost_system boost_log boost_log_setup)

add_executable(event_manager_tests  persistence_tests.cpp event_manager_tests.cpp )
target_link_libraries(event_manager_tests gtest mico_rdf mico_marmotta mico_persistence ${AMQPCPP_LIBRARIES} mico_event curl ${PROTOBUF_LIBRARIES} boost_system boost_log boost_log_setup)

add_executable(uuid_tests uuid_tests.cpp)
target_link_libraries(uuid_tests gtest pthread)


