add_library(mico_rdf SHARED rdf_model.cpp rdf_query_tuple.cpp rdf_query_bool.cpp)
add_library(mico_marmotta SHARED http_client.cpp sparql_client.cpp)

target_link_libraries(mico_rdf ${EXPAT_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT})
target_link_libraries(mico_marmotta mico_rdf)

install (TARGETS mico_rdf mico_marmotta DESTINATION lib)
install (FILES http_client.hpp rdf_model.hpp sparql_client.hpp DESTINATION include)