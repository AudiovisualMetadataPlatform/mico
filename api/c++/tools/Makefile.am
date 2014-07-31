LIBRDF=../rdf/libmico_rdf.la
LIBMARMOTTA=../rdf/libmico_marmotta.la
LIBPERSISTENCE=../persistence/libmico_persistence.la
LIBAMQP=../amqp/libmico_amqp.la
LIBEVENT=../event/libmico_event.la

AM_CXXFLAGS=-I../rdf -I../persistence -I../event -I../amqp

bin_PROGRAMS=

if TOOL_INJECT
bin_PROGRAMS += mico_inject
mico_inject_SOURCES = mico_inject.cpp
mico_inject_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBMARMOTTA) $(LIBAMQP) $(LIBEVENT) @curllibs@ @protobuf_libs@ @magiclibs@ -lboost_system -lboost_log -lboost_log_setup
endif