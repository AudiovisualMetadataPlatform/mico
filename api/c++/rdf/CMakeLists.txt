add_library(mico_rdf SHARED rdf_model.cpp rdf_query_tuple.cpp rdf_query_bool.cpp)
add_library(mico_marmotta SHARED http_client.cpp sparql_client.cpp)


install (TARGETS mico_rdf mico_marmotta DESTINATION lib)
install (FILES http_client.hpp rdf_model.hpp sparql_client.hpp DESTINATION include)