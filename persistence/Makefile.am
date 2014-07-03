AM_CXXFLAGS=-I../rdf/src/main/c++

# shared C++ libraries
lib_LTLIBRARIES = libmico_persistence.la
libmico_persistence_la_SOURCES = src/main/c++/http_client.cpp
libmico_persistence_la_LIBADD = ../rdf/libmico_rdf.la @curllibs@
include_HEADERS = src/main/c++/http_client.hpp


noinst_PROGRAMS=
if GTEST
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = src/test/c++/gtest-all.cc

noinst_PROGRAMS += client_tests 
client_tests_SOURCES = src/test/c++/persistence_tests.cpp src/test/c++/http_request_tests.cpp src/test/c++/http_client_tests.cpp 
client_tests_LDADD = ../rdf/libmico_rdf.la libmico_persistence.la libgtest.a @curllibs@

endif
