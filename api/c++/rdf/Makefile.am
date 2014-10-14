# shared C++ libraries
lib_LTLIBRARIES = libmico_rdf.la libmico_marmotta.la
libmico_rdf_la_SOURCES = rdf_model.cpp rdf_query_tuple.cpp rdf_query_bool.cpp 
libmico_rdf_la_LIBADD = @xmllibs@
libmico_marmotta_la_SOURCES = http_client.cpp sparql_client.cpp 
libmico_marmotta_la_LIBADD = libmico_rdf.la @curllibs@
include_HEADERS = rdf_model.hpp rdf_query.hpp http_client.hpp sparql_client.hpp vocabularies.hpp


