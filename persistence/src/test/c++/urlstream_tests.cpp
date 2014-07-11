#include "gtest.h"

#include <string>
#include <iostream>

#include "URLStream.hpp"


using namespace mico::io;

TEST(URLStreamTests,HTTPReadStream) {
	url_istream is("https://s36cuibb4qf5.runscope.net/");
	
	std::string r;
	while(is) {
		std::string s;
		is >> s;
		
		r +=s;
	}
	std::cout << "Data:\n" << r << "\n===\n";
	ASSERT_TRUE(r.length() > 0);
}

TEST(URLStreamTests,HTTPWriteStream) {
	url_ostream os("https://s36cuibb4qf5.runscope.net/");
	
	os << "Hello, World!\n";
	os << "Sending through URLStream\n";
	
	std::cout << "flushing...\n";
	os.flush();
}
