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

TEST(URLStreamTests,HDFSWriteStream) {
	url_ostream os("hdfs://127.0.0.1/test.txt");

	os << "Hello, World!\n";
	os << "Sending through URLStream\n";

	os.flush();
}

TEST(URLStreamTests,HDFSReadStream) {
	url_istream is("hdfs://127.0.0.1/test.txt");

	std::string r;
	while(is) {
		std::string s;
		is >> s;

		r +=s;
	}
	ASSERT_TRUE(r.length() > 0);
}
