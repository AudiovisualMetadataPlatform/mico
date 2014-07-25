LIBRDF=../rdf/libmico_rdf.la
LIBMARMOTTA=../rdf/libmico_marmotta.la
LIBPERSISTENCE=../persistence/libmico_persistence.la

AM_CXXFLAGS=-I../rdf -I../persistence -I../event
noinst_PROGRAMS=
TESTS=
if GTEST
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = gtest-all.cc

noinst_PROGRAMS += model_tests query_tests client_tests sparql_tests metadata_tests content_item_tests persistence_service_tests urlstream_tests
model_tests_SOURCES = rdf_tests.cpp uri_tests.cpp bnode_tests.cpp literal_tests.cpp 
model_tests_LDADD = libgtest.a $(LIBRDF)

query_tests_SOURCES = rdf_tests.cpp sparql_parse_tests.cpp
query_tests_LDADD = libgtest.a $(LIBRDF)

client_tests_SOURCES = rdf_tests.cpp http_request_tests.cpp http_client_tests.cpp 
client_tests_LDADD = $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@

sparql_tests_SOURCES = rdf_tests.cpp sparql_client_tests.cpp 
sparql_tests_LDADD = $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@

persistence_service_tests_SOURCES = persistence_tests.cpp persistence_service_tests.cpp 
persistence_service_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA)  libgtest.a @curllibs@ 

metadata_tests_SOURCES = persistence_tests.cpp metadata_tests.cpp 
metadata_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ 

content_item_tests_SOURCES = persistence_tests.cpp content_item_tests.cpp
content_item_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ 

urlstream_tests_SOURCES = persistence_tests.cpp urlstream_tests.cpp
urlstream_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@

TESTS += model_tests query_tests client_tests sparql_tests metadata_tests content_item_tests persistence_service_tests urlstream_tests
endif
