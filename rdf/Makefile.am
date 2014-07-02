# shared C++ libraries
lib_LTLIBRARIES = libmico_rdf.la
libmico_rdf_la_SOURCES = src/main/c++/rdf_model.cpp src/main/c++/rdf_query.cpp
libmico_rdf_la_LIBADD = @xmllibs@
include_HEADERS = src/main/c++/rdf_model.hpp src/main/c++/rdf_query.hpp


AM_CXXFLAGS=
noinst_PROGRAMS=
if GTEST
AM_CXXFLAGS += -I$(GTEST_DIR)/include
noinst_PROGRAMS += model_tests query_tests
model_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/uri_tests.cpp src/test/c++/bnode_tests.cpp src/test/c++/literal_tests.cpp 
model_tests_LDADD = -L$(GTEST_DIR)/lib -lgtest libmico_rdf.la

query_tests_SOURCES = src/test/c++/rdf_tests.cpp src/test/c++/sparql_parse_tests.cpp
query_tests_LDADD = -L$(GTEST_DIR)/lib -lgtest libmico_rdf.la

endif
