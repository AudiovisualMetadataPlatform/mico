#include "gtest.h"
#include "../../main/c++/rdf_model.hpp"

using namespace org::openrdf::model;

TEST(URITest, CSTRAssignment) {
  URI u1 = "http://localhost/resource/u1";

  EXPECT_STREQ("http://localhost/resource/u1", u1.stringValue().c_str());
}

TEST(URITest, StringAssignment) {
  std::string s("http://localhost/resource/u1"); 

  URI u1 = s;

  EXPECT_EQ(s, u1.stringValue());
}


TEST(URITest, URIAssignment) {
  URI u1 = "http://localhost/resource/u1";
  URI u2 = u1;

  EXPECT_EQ(u1.stringValue(), u2.stringValue());
  EXPECT_NE(&u1,&u2);
}


TEST(URITest, LocalName) {
  URI u1 = "http://localhost/resource/u1";

  EXPECT_STREQ("u1", u1.getLocalName().c_str());

  URI u2 = "http://localhost/resource#u2";

  EXPECT_STREQ("u2", u2.getLocalName().c_str());
}


TEST(URITest, Namespace) {
  URI u1 = "http://localhost/resource/u1";

  EXPECT_STREQ("http://localhost/resource/", u1.getNamespace().c_str());

  URI u2 = "http://localhost/resource#u2";

  EXPECT_STREQ("http://localhost/resource#", u2.getNamespace().c_str());
}


TEST(URITest, Equals) {
  URI u1 = "http://localhost/resource/u1";
  URI u2 = "http://localhost/resource/u1";
  URI u3 = "http://localhost/resource/u3";

  EXPECT_EQ(u1,u2);
  EXPECT_NE(u1,u3);

  // implicit casting
  EXPECT_EQ(u1,"http://localhost/resource/u1");
  EXPECT_EQ("http://localhost/resource/u1", u1);
  EXPECT_NE(u1,"http://localhost/resource/u2");
  EXPECT_NE("http://localhost/resource/u2", u1);
}


