AM_CXXFLAGS=-I../rdf/src/main/c++

# shared C++ libraries
lib_LTLIBRARIES = libmico_persistence.la
libmico_persistence_la_SOURCES = 
libmico_persistence_la_LIBADD = ../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la
include_HEADERS = 


noinst_PROGRAMS=
if GTEST
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = src/test/c++/gtest-all.cc

endif
