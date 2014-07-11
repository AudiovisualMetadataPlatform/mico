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
	//std::cout << "Data:\n" << r << "\n===\n";
	ASSERT_TRUE(r.length() > 0);
}

TEST(URLStreamTests,HTTPWriteStream) {
	url_ostream os("https://s36cuibb4qf5.runscope.net/");
	
	os << "Hello, World!\n";
	os << "Sending through URLStream\n";
	
	os.flush();
}


TEST(URLStreamTests,FILEReadStream) {
	url_istream is("file:///etc/profile");
	
	std::string r;
	while(is) {
		std::string s;
		is >> s;
		
		r +=s;
	}
	ASSERT_TRUE(r.length() > 0);
}


TEST(URLStreamTests,FILEWriteStream) {
	url_ostream os("file:///tmp/urlstream/test.txt");
	
	os << "Hello, World!\n";
	os << "Sending through URLStream\n";
	
	os.flush();
}
