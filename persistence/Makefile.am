# "compile" SPARQL files into C arrays so they can be included
src/main/c++/%.sparql.cc: src/main/resources/sparql/%.sparql
	$(XXD) -i $< $@


clean-local:
	rm -f src/main/c++/*.sparql.cc

AM_CXXFLAGS=-I../rdf/src/main/c++

# shared C++ libraries
lib_LTLIBRARIES = libmico_persistence.la
libmico_persistence_la_SOURCES=src/main/c++/createContentItem.sparql.cc src/main/c++/deleteContentItem.sparql.cc src/main/c++/askContentItem.sparql.cc src/main/c++/listContentItems.sparql.cc src/main/c++/createContentPart.sparql.cc src/main/c++/deleteContentPart.sparql.cc src/main/c++/askContentPart.sparql.cc src/main/c++/listContentParts.sparql.cc src/main/c++/deleteGraph.sparql.cc src/main/c++/SPARQLUtil.cpp src/main/c++/Metadata.cpp src/main/c++/Content.cpp src/main/c++/ContentItem.cpp src/main/c++/PersistenceService.cpp src/main/c++/URLStream.cpp
libmico_persistence_la_LIBADD=../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la 
include_HEADERS = src/main/c++/Content.hpp src/main/c++/ContentItem.hpp src/main/c++/Metadata.hpp src/main/c++/PersistenceService.hpp src/main/c++/URLStream.hpp


noinst_PROGRAMS=
TESTS =
if GTEST
AM_CXXFLAGS += -Isrc/main/c++
noinst_PROGRAMS  += metadata_tests content_item_tests persistence_service_tests urlstream_tests
TESTS            += metadata_tests content_item_tests persistence_service_tests urlstream_tests
noinst_LIBRARIES = libgtest.a
libgtest_a_SOURCES = src/test/c++/gtest-all.cc

persistence_service_tests_SOURCES = src/test/c++/persistence_tests.cpp src/test/c++/persistence_service_tests.cpp 
persistence_service_tests_LDADD = libmico_persistence.la ../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la libgtest.a @curllibs@ 

metadata_tests_SOURCES = src/test/c++/persistence_tests.cpp src/test/c++/metadata_tests.cpp 
metadata_tests_LDADD = libmico_persistence.la ../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la libgtest.a @curllibs@ 

content_item_tests_SOURCES = src/test/c++/persistence_tests.cpp src/test/c++/content_item_tests.cpp
content_item_tests_LDADD = libmico_persistence.la ../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la libgtest.a @curllibs@ 

urlstream_tests_SOURCES = src/test/c++/persistence_tests.cpp src/test/c++/urlstream_tests.cpp
urlstream_tests_LDADD = libmico_persistence.la ../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la libgtest.a @curllibs@

endif


