# shared C++ libraries
lib_LTLIBRARIES = libmico_rdf.la
libmico_rdf_la_SOURCES = src/main/c++/rdf_model.cpp
include_HEADERS = src/main/c++/rdf_model.hpp


AM_CXXFLAGS=
noinst_PROGRAMS=
if GTEST
AM_CXXFLAGS += -I$(GTEST_DIR)/include
noinst_PROGRAMS += model_tests
model_tests_SOURCES = src/test/c++/model_tests.cpp src/test/c++/uri_tests.cpp src/test/c++/bnode_tests.cpp src/test/c++/literal_tests.cpp 
model_tests_LDADD = -L$(GTEST_DIR)/lib -lgtest libmico_rdf.la
endif
