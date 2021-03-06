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
#include "gtest.h"
#include "rdf_model.hpp"

using namespace mico::rdf::model;

TEST(LiteralTest, CSTRAssignment) {
  Literal l1 = "Hello, World!";

  EXPECT_STREQ("Hello, World!", l1.stringValue().c_str());
}

TEST(LiteralTest, StringAssignment) {
  std::string s("Hello, World!"); 

  Literal l1 = s;

  EXPECT_EQ(s, l1.stringValue());
}

TEST(LiteralTest, IntAssignment) {
  Literal l1 = 10;

  EXPECT_STREQ("10", l1.stringValue().c_str());
}

TEST(LiteralTest, BoolAssignment) {
  Literal l1 = true;
  Literal l2 = false;

  EXPECT_STREQ("true", l1.stringValue().c_str());
  EXPECT_STREQ("false", l2.stringValue().c_str());
}



TEST(LiteralTest, LiteralAssignment) {
  Literal u1 = "Hello, World!";
  Literal u2 = u1;

  EXPECT_EQ(u1.stringValue(), u2.stringValue());
  EXPECT_NE(&u1,&u2);
}


TEST(LiteralTest, NumberCasting) {
  Literal l1 = "10";
  EXPECT_EQ(10, l1.intValue());
  EXPECT_EQ(10, l1.shortValue());
  EXPECT_EQ(10, l1.byteValue());
  EXPECT_EQ(10.0, l1.doubleValue());
  EXPECT_EQ(10.0, l1.floatValue());
}


TEST(LiteralTest, BooleanCasting) {
  Literal l1 = "true";
  Literal l2 = "false";
  EXPECT_TRUE(l1.booleanValue());
  EXPECT_FALSE(l2.booleanValue());
}


TEST(LiteralTest, IntDatatypeLiteral) {
  DatatypeLiteral l1 = 10;

  EXPECT_EQ(10, l1.intValue());
  EXPECT_STREQ("http://www.w3.org/2001/XMLSchema#int", l1.getDatatype().stringValue().c_str());

}


TEST(LiteralTest, DoubleDatatypeLiteral) {

  DatatypeLiteral l2 = 10.0;
  EXPECT_EQ(10.0, l2.doubleValue());
  EXPECT_STREQ("http://www.w3.org/2001/XMLSchema#double", l2.getDatatype().stringValue().c_str());

}

TEST(LiteralTest, BooleanDatatypeLiteral) {

  DatatypeLiteral l3 = true;
  EXPECT_TRUE(l3.booleanValue());
  EXPECT_TRUE(l3);
  EXPECT_STREQ("http://www.w3.org/2001/XMLSchema#boolean", l3.getDatatype().stringValue().c_str());

}


TEST(LiteralTest, PlainLiteralEquals) {
  Literal u1 = "Hello, World!";
  Literal u2 = "Hello, World!";
  Literal u3 = "Hallo, Welt!";

  EXPECT_EQ(u1,u2);
  EXPECT_NE(u1,u3);

  EXPECT_STREQ((const char*)u1, "Hello, World!");
  EXPECT_STREQ("Hello, World!", (const char*)u1);
}

TEST(LiteralTest, LanguageLiteralEquals) {
  LanguageLiteral u1("Hello, World!","en");
  LanguageLiteral u2("Hello, World!","en");
  LanguageLiteral u3("Hallo, Welt!","de");

  EXPECT_EQ(u1,u2);
  EXPECT_NE(u1,u3);

  EXPECT_STREQ((const char*)u1, "Hello, World!");
  EXPECT_STREQ("Hello, World!", (const char*)u1);
}


TEST(LiteralTest, DatatypeLiteralEquals) {
  DatatypeLiteral l1("10","http://www.w3.org/2001/XMLSchema#int");
  DatatypeLiteral l2("10","http://www.w3.org/2001/XMLSchema#int");
  DatatypeLiteral l3("10","http://www.w3.org/2001/XMLSchema#double");

  EXPECT_EQ(l1,l2);
  EXPECT_NE(l1,l3);

  EXPECT_STREQ((const char*)l1, "10");
  EXPECT_STREQ("10", (const char*)l1);

  EXPECT_EQ((int)l1, 10);
  EXPECT_EQ(10, (int)l1);

  EXPECT_EQ((double)l1, 10.0);
  EXPECT_EQ(10.0, (double)l1);
}
