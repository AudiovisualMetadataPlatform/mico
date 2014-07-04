# shared C++ libraries
lib_LTLIBRARIES = libmico_rdf.la libmico_marmotta.la
libmico_rdf_la_SOURCES = src/main/c++/rdf_model.cpp src/main/c++/rdf_query_tuple.cpp src/main/c++/rdf_query_bool.cpp 
libmico_rdf_la_LIBADD = @xmllibs@
libmico_marmotta_la_SOURCES = src/main/c++/http_client.cpp src/main/c++/sparql_client.cpp 
libmico_marmotta_la_LIBADD = libmico_rdf.la @curllibs@
include_HEADERS = src/main/c++/rdf_model.hpp src/main/c++/rdf_query.hpp src/main/c++/http_client.hpp src/main/c++/sparql_client.hpp


AM_CXXFLAGS=
noinst_PROGRAMS=
if GTEST
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = src/test/c++/gtest-all.cc

noinst_PROGRAMS += model_tests query_tests client_tests sparql_tests
model_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/uri_tests.cpp src/test/c++/bnode_tests.cpp src/test/c++/literal_tests.cpp 
model_tests_LDADD = libgtest.a libmico_rdf.la

query_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/sparql_parse_tests.cpp
query_tests_LDADD = libgtest.a libmico_rdf.la

client_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/http_request_tests.cpp src/test/c++/http_client_tests.cpp 
client_tests_LDADD = libmico_rdf.la libmico_marmotta.la libgtest.a @curllibs@

sparql_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/sparql_client_tests.cpp 
sparql_tests_LDADD = libmico_rdf.la libmico_marmotta.la libgtest.a @curllibs@

endif
