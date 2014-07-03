#include <curl/curl.h>
#include "gtest.h"
#include "../../main/c++/http_client.hpp"

using namespace eu::mico::http;

TEST(HTTPClientTest, TestGET) {
  Request req(GET,"http://www.mico-project.eu");
  
  HTTPClient client;

  Response* resp = client.execute(req);

  std::cout << *(Message*)resp;
}
