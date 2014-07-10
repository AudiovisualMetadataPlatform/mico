#include "gtest.h"

#include <string>
#include <iostream>

#include "URLStream.hpp"


using namespace mico::io;

TEST(URLStreamTests,HTTPOpenStream) {
	url_istream is("https://w2rqo92gnzay.runscope.net");
	
	while(is) {
		std::string s;
		is >> s;
		std::cout << s;
	}
}
