#include "gtest.h"

#include <iostream>
#include <sstream>
#include <fstream>
#include <map>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/algorithm/string.hpp>

#include "http_client.hpp"

#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"

#include "../config.h"

using namespace std;
using namespace boost::uuids;
using namespace mico::persistence;
using namespace mico::http;
using namespace mico::util;


#define VBOX_SERVER std::string(TEST_HOST)


class ContentItemTest : public ::testing::Test
{

protected:
	std::string base_url = "http://" + VBOX_SERVER + ":8080/marmotta";
	std::string content_dir = "ftp://mico:mico@" + VBOX_SERVER;
	uuid base_ctx;
	random_generator rnd_gen;
	HTTPClient client;
	ContentItem* item;

	virtual void SetUp() {
		base_ctx = rnd_gen();
		item = new ContentItem(base_url, content_dir, base_ctx);
	}


	virtual void TearDown() {
		// delete pre-loaded data
		Request req(DELETE,base_url+"/context?graph="+base_url+"/"+boost::uuids::to_string(base_ctx));

		Response* resp = client.execute(req);
		delete resp;

		delete item;
	}

	void assertAskM(std::string query) {
		ASSERT_TRUE(item->getMetadata().ask(query));
	}
	void assertAskMN(std::string query) {
		ASSERT_FALSE(item->getMetadata().ask(query));
	}

	void assertAskE(std::string query) {
		ASSERT_TRUE(item->getExecution().ask(query));
	}

	void assertAskR(std::string query) {
		ASSERT_TRUE(item->getResult().ask(query));
	}

};

template<typename T>
bool ptr_contains(T* ptr, T** arr, int len)
{
	for(int i=0; i<len; i++) {
		if(*ptr == *arr[i]) {
			return true;
		}
	}
	return false;
}

TEST_F(ContentItemTest,ContentItemMetadata)
{
	ContentItemMetadata& m = item->getMetadata();

	m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
	assertAskM("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}

TEST_F(ContentItemTest,ExecutionMetadata)
{
	ExecutionMetadata& m = item->getExecution();

	m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
	assertAskE("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}

TEST_F(ContentItemTest,ResultMetadata)
{
	ResultMetadata& m = item->getResult();

	m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
	assertAskR("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}


TEST_F(ContentItemTest,CreateDeleteContentPart)
{

	ASSERT_EQ(item->begin(), item->end());

	Content* part = item->createContentPart();

	ASSERT_NE(item->begin(), item->end());

	map<string,string> params;
	params["ci"] = item->getURI().stringValue();
	params["cp"] = part->getURI().stringValue();

	assertAskM(sparql_format_query("ASK { <$(ci)> <http://www.w3.org/ns/ldp#contains> <$(cp)> }",params));

	item->deleteContentPart(part->getURI());

	ASSERT_EQ(item->begin(), item->end());
	assertAskMN(sparql_format_query("ASK { <$(ci)> <http://www.w3.org/ns/ldp#contains> <$(cp)> }",params));

	delete part;
}


TEST_F(ContentItemTest,ListContentParts)
{

	ASSERT_EQ(item->begin(), item->end());


	Content* parts[5];
	for(int i=0; i<5; i++) {
		parts[i] = item->createContentPart();
	}

	ASSERT_NE(item->begin(), item->end());

	for(Content* part : *item) {
		ASSERT_TRUE(ptr_contains(part,parts,5));
	}


}


TEST_F(ContentItemTest,SetType)
{
	Content* part = item->createContentPart();

	part->setType("text/plain");
	
	ASSERT_EQ("text/plain", part->getType());

	item->deleteContentPart(part->getURI());
	delete part;
}

TEST_F(ContentItemTest,StreamContentPart)
{

	ASSERT_EQ(item->begin(), item->end());

	Content* part = item->createContentPart();

	// write some data
	std::ostream* os = part->getOutputStream();
	*os << "Hello World\n";
	delete os;

	// read data again
	std::istream* is = part->getInputStream();
	ASSERT_TRUE(*is);
	std::string s_hello;
	*is >> s_hello;
	ASSERT_STREQ("Hello",s_hello.c_str());
	std::string s_world;
	*is >> s_world;
	ASSERT_STREQ("World",s_world.c_str());
	delete is;

	item->deleteContentPart(part->getURI());


	delete part;
}
