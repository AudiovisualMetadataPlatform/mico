AM_CXXFLAGS=-I../rdf -I../persistence -I../amqp

# shared C++ libraries
lib_LTLIBRARIES = libmico_event.la
libmico_event_la_SOURCES = Event.pb.cc EventManager.cpp
libmico_event_la_LIBADD=../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la ../persistence/libmico_persistence.la ../amqp/libmico_amqp.la @protobuf_libs@ -lboost_system -lboost_log  @curllibs@
include_HEADERS = AnalysisService.hpp EventManager.hpp Event.pb.h
