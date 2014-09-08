# "compile" SPARQL files into C arrays so they can be included
%.sparql.cc: ../../shared/persistence/sparql/%.sparql
	$(XXD) -i $< $@


clean-local:
	rm -f *.sparql.cc

AM_CXXFLAGS=-I../rdf

# shared C++ libraries
lib_LTLIBRARIES = libmico_persistence.la
libmico_persistence_la_SOURCES=setContentRelation.sparql.cc getContentRelation.sparql.cc setContentProperty.sparql.cc getContentProperty.sparql.cc setContentType.sparql.cc getContentType.sparql.cc createContentItem.sparql.cc deleteContentItem.sparql.cc askContentItem.sparql.cc listContentItems.sparql.cc createContentPart.sparql.cc deleteContentPart.sparql.cc askContentPart.sparql.cc listContentParts.sparql.cc deleteGraph.sparql.cc SPARQLUtil.cpp Metadata.cpp Content.cpp ContentItem.cpp PersistenceService.cpp URLStream.cpp
libmico_persistence_la_LIBADD=../rdf/libmico_rdf.la ../rdf/libmico_marmotta.la -lboost_system -lboost_log  @curllibs@
include_HEADERS = Content.hpp ContentItem.hpp Metadata.hpp PersistenceService.hpp URLStream.hpp


