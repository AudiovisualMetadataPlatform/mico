add_library(mico_rdf SHARED rdf_model.cpp rdf_query_tuple.cpp rdf_query_bool.cpp)
add_library(mico_marmotta SHARED http_client.cpp sparql_client.cpp)