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

TEST(BNodeTest, Generated) {
  BNode b1;

  EXPECT_STRNE("", b1.stringValue().c_str());
}


TEST(BNodeTest, CSTRAssignment) {
  BNode b1 = "_b1";

  EXPECT_STREQ("_b1", b1.stringValue().c_str());
}

TEST(BNodeTest, StringAssignment) {
  std::string s("_b1"); 

  BNode b1 = s;

  EXPECT_EQ(s, b1.stringValue());
}


TEST(BNodeTest, BNodeAssignment) {
  BNode b1 = "_b1";
  BNode b2 = b1;

  EXPECT_EQ(b1.stringValue(), b2.stringValue());
  EXPECT_NE(&b1,&b2);
}

TEST(BNodeTest, Equals) {
  BNode u1 = "_u1";
  BNode u2 = "_u1";
  BNode u3;

  EXPECT_EQ(u1,u2);
  EXPECT_NE(u1,u3);
}


