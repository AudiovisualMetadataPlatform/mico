LIBRDF=../rdf/libmico_rdf.la
LIBMARMOTTA=../rdf/libmico_marmotta.la
LIBPERSISTENCE=../persistence/libmico_persistence.la
LIBAMQP=../amqp/libmico_amqp.la
LIBEVENT=../event/libmico_event.la
BOOST_LIBS=-lboost_system -lboost_log -lboost_log_setup

AM_CXXFLAGS=-I../rdf -I../persistence -I../event -I../amqp
noinst_PROGRAMS=
TESTS=
if GTEST
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = gtest-all.cc

noinst_PROGRAMS += model_tests query_tests client_tests sparql_tests metadata_tests content_item_tests persistence_service_tests urlstream_tests event_manager_tests
model_tests_SOURCES = rdf_tests.cpp uri_tests.cpp bnode_tests.cpp literal_tests.cpp 
model_tests_LDADD = libgtest.a $(LIBRDF)

query_tests_SOURCES = rdf_tests.cpp sparql_parse_tests.cpp
query_tests_LDADD = libgtest.a $(LIBRDF)

client_tests_SOURCES = rdf_tests.cpp http_request_tests.cpp http_client_tests.cpp 
client_tests_LDADD = $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ $(BOOST_LIBS)

sparql_tests_SOURCES = rdf_tests.cpp sparql_client_tests.cpp 
sparql_tests_LDADD = $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ $(BOOST_LIBS)

persistence_service_tests_SOURCES = persistence_tests.cpp persistence_service_tests.cpp 
persistence_service_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA)  libgtest.a @curllibs@ $(BOOST_LIBS)

metadata_tests_SOURCES = persistence_tests.cpp metadata_tests.cpp 
metadata_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ $(BOOST_LIBS)

content_item_tests_SOURCES = persistence_tests.cpp content_item_tests.cpp
content_item_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ $(BOOST_LIBS)

urlstream_tests_SOURCES = persistence_tests.cpp urlstream_tests.cpp
urlstream_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) libgtest.a @curllibs@ $(BOOST_LIBS)

event_manager_tests_SOURCES = persistence_tests.cpp event_manager_tests.cpp
event_manager_tests_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) $(LIBAMQP) $(LIBEVENT) libgtest.a @curllibs@ @protobuf_libs@ -lboost_system -lboost_log -lboost_log_setup


TESTS += model_tests query_tests client_tests sparql_tests metadata_tests content_item_tests persistence_service_tests urlstream_tests event_manager_tests
endif
