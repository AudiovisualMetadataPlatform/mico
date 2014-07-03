#include <curl/curl.h>
#include "gtest.h"
#include "../../main/c++/http_client.hpp"

using namespace eu::mico::http;

TEST(HTTPClientTest, TestGET) {
  Request req(GET,"http://www.mico-project.eu");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_TRUE(resp->getBody() != NULL);
  EXPECT_TRUE(resp->getBody()->getContentLength() > 0);
  EXPECT_EQ("text/html; charset=UTF-8", resp->getHeader("Content-Type"));
}


TEST(HTTPClientTest, TestPOST) {
  Request req(POST,"http://posttestserver.com/post.php");
  req.setBody("Hello, World!","text/plain");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  EXPECT_EQ(200, resp->getStatus());
}
