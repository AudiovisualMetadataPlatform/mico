#include <curl/curl.h>
#include "gtest.h"
#include "../../main/c++/http_client.hpp"

using namespace mico::http;

TEST(HTTPClientTest, TestGET) {
  Request req(GET,"https://w2rqo92gnzay.runscope.net");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("application/json; charset=utf-8", resp->getHeader("Content-Type"));
}


TEST(HTTPClientTest, TestPOST) {
  Request req(POST,"https://w2rqo92gnzay.runscope.net");
  req.setBody("Hello, World!","text/plain");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("application/json; charset=utf-8", resp->getHeader("Content-Type"));
}

TEST(HTTPClientTest, TestPUT) {
  Request req(PUT,"https://w2rqo92gnzay.runscope.net");
  req.setBody("Hello, World!","text/plain");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("application/json; charset=utf-8", resp->getHeader("Content-Type"));
}


TEST(HTTPClientTest, TestDELETE) {
  Request req(DELETE,"https://w2rqo92gnzay.runscope.net");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("application/json; charset=utf-8", resp->getHeader("Content-Type"));
}


TEST(HTTPClientTest, TestHeaders) {
  Request req(POST,"https://w2rqo92gnzay.runscope.net");
  req.setHeader("X-Mico-Foo","bar");
  req.setBody("Testing Headers","text/plain");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("application/json; charset=utf-8", resp->getHeader("Content-Type"));
}
