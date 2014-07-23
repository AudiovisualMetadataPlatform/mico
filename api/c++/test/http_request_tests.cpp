#include <curl/curl.h>
#include "gtest.h"
#include "http_client.hpp"

using namespace mico::http;

TEST(HTTPRequestTest, TestBodyPtr) {
  char* data = "Hello, World!";
  Body  body(data,strlen(data),"text/plain");

  EXPECT_STREQ(data, (const char*)body.getContent());
  EXPECT_STREQ("text/plain", body.getContentType().c_str());
  EXPECT_EQ(13, body.getContentLength());
}


TEST(HTTPRequestTest, TestBodyStr) {
  std::string data = "Hello, World!";
  Body  body(data,"text/plain");

  EXPECT_STREQ(data.c_str(), (const char*)body.getContent());
  EXPECT_STREQ("text/plain", body.getContentType().c_str());
  EXPECT_EQ(13, body.getContentLength());
}


TEST(HTTPRequestTest, TestRequestConstruct) {
  Request req(POST,"http://localhost");
  
  EXPECT_EQ("http://localhost", req.getURL());
  EXPECT_EQ(POST, req.getMethod());
}


TEST(HTTPRequestTest, TestRequestHeaders) {
  Request req(POST,"http://localhost");

  req.setHeader("X-MICO-Test","foo");
    
  EXPECT_EQ("foo", req.getHeader("X-MICO-Test"));
  
  curl_slist* cheaders = req.getCurlHeaders();
  ASSERT_TRUE(cheaders != NULL);
  EXPECT_STREQ("Expect: ",cheaders->data);
  ASSERT_TRUE(cheaders->next != NULL);
  EXPECT_STREQ("X-MICO-Test: foo",cheaders->next->data);

}


TEST(HTTPRequestTest, TestRequestCurlHeaders) {
  Request req(POST,"http://localhost");

  curl_slist* cheaders = NULL;
  cheaders = curl_slist_append(cheaders,"X-MICO-Test: foo");
  req.setCurlHeaders(cheaders);
    
  EXPECT_EQ("foo", req.getHeader("X-MICO-Test"));
  
  curl_slist* cheaders2 = req.getCurlHeaders();
  ASSERT_TRUE(cheaders2 != NULL);
  EXPECT_STREQ("Expect: ",cheaders2->data);
  ASSERT_TRUE(cheaders2->next != NULL);
  EXPECT_STREQ("X-MICO-Test: foo",cheaders2->next->data);

  curl_slist_free_all(cheaders);
  curl_slist_free_all(cheaders2);
}


TEST(HTTPRequestTest, TestRequestBodyPtr) {
  Request req(POST,"http://localhost");
  
  char* data = "Hello, World!";
  req.setBody(data,strlen(data),"text/plain");

  EXPECT_EQ("http://localhost", req.getURL());
  EXPECT_EQ(POST, req.getMethod());
  EXPECT_STREQ(data, (const char*)req.getBody()->getContent());
  EXPECT_STREQ("text/plain", req.getBody()->getContentType().c_str());
  EXPECT_EQ(13, req.getBody()->getContentLength());
}


TEST(HTTPRequestTest, TestRequestBodyStr) {
  Request req(POST,"http://localhost");
  
  std::string data = "Hello, World!";
  req.setBody(data,"text/plain");

  EXPECT_EQ("http://localhost", req.getURL());
  EXPECT_EQ(POST, req.getMethod());
  EXPECT_STREQ(data.c_str(), (const char*)req.getBody()->getContent());
  EXPECT_STREQ("text/plain", req.getBody()->getContentType().c_str());
  EXPECT_EQ(13, req.getBody()->getContentLength());

}
