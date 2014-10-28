/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <iostream>
#include <sstream>
#include "gtest.h"
#include "rdf_model.hpp"
#include "rdf_query.hpp"


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


const char* bool_result =
  "<?xml version=\"1.0\"?>"
  "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"
  "  <boolean>true</boolean>"
  "</sparql>";


using namespace std;
using namespace mico::rdf::query;
using namespace mico::rdf::model;

TEST(SPARQLParse,ParseTupleResultStream) {
  std::stringstream ss;
  ss << sample_result;


  TupleResult r;
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


TEST(SPARQLParse,ParseTupleResultMem) {

  TupleResult r;
  r.loadFrom(sample_result,strlen(sample_result));

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


TEST(SPARQLParse,ParseBooleanResultStream) {
  std::stringstream ss;
  ss << bool_result;

  BooleanResult r;
  ss >> r;

  ASSERT_TRUE(r);
}


TEST(SPARQLParse,ParseBooleanResultMem) {
  BooleanResult r;
  r.loadFrom(bool_result,strlen(bool_result));

  ASSERT_TRUE(r);
}

