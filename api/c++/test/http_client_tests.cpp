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
#include <curl/curl.h>
#include "gtest.h"
#include "http_client.hpp"

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
