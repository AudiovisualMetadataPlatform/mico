#include <iostream>
#include <sstream>
#include "gtest.h"
#include "../../main/c++/rdf_model.hpp"
#include "../../main/c++/rdf_query.hpp"


const char* sample_result =
  "<?xml version=\"1.0\"?>"
  "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"
  "  <head>"
  "    <variable name=\"x\"/>"
  "    <variable name=\"hpage\"/>"
  "    <variable name=\"name\"/>"
  "    <variable name=\"age\"/>"
  "    <variable name=\"mbox\"/>"
  "    <variable name=\"friend\"/>"
  "  </head>"
  "  <results>"
  "    <result> "
  "      <binding name=\"x\">"
  "	<bnode>r2</bnode>"
  "      </binding>"
  "      <binding name=\"hpage\">"
  "	<uri>http://work.example.org/bob/</uri>"
  "      </binding>"
  "      <binding name=\"name\">"
  "	<literal xml:lang=\"en\">Bob</literal>"
  "      </binding>"
  "      <binding name=\"age\">"
  "	<literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">30</literal>"
  "      </binding>"
  "      <binding name=\"mbox\">"
  "	<uri>mailto:bob@work.example.org</uri>"
  "      </binding>"
  "    </result>"
  "  </results>"
  "</sparql>";


using namespace org::openrdf::query;
using namespace org::openrdf::model;

TEST(SPARQLParse,ParseTupleResult) {
  std::stringstream ss;
  ss << sample_result;


  QueryResult r;
  ss>>r;

  const string vars[] = { "x", "hpage", "name", "age", "mbox", "friend" };
  const Value*  vals[] = { 
    new BNode("r2"), 
    new URI("http://work.example.org/bob/"), 
    new LanguageLiteral("Bob","en"), 
    new DatatypeLiteral("30","http://www.w3.org/2001/XMLSchema#integer"), 
    new URI("mailto:bob@work.example.org") 
  };

  ASSERT_EQ(6,r.getBindingNames().size());
  for(int i=0; i<6; i++) {
    ASSERT_EQ(vars[i],r.getBindingNames()[i]);
  }

  ASSERT_EQ(1, r.size());
  for(int i=0; i<5; i++) {
    ASSERT_EQ(*((Value*)vals[i]),*((Value*)r[0][vars[i]]));
  }
}
