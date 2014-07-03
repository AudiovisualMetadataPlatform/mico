#include "gtest.h"
#include "../../main/c++/rdf_model.hpp"

using namespace org::openrdf::model;

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


