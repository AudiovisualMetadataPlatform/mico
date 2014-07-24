#ifndef HAVE_CONTENT_H
#define HAVE_CONTENT_H 1

#include <string>
#include <iostream>
#include "rdf_model.hpp"


namespace mico
{
namespace persistence
{

using std::string;
using namespace mico::rdf::model;

class ContentItem;

class Content
{
	friend bool operator==(Content& c1, Content& c2);

protected:
	ContentItem& item;
	string baseUrl;
	string id;
	const string& contentDirectory;

public:
	Content(ContentItem& item, const string baseUrl, const string& contentDirectory, const string id) : item(item), baseUrl(baseUrl), id(id), contentDirectory(contentDirectory) {};

	Content(ContentItem& item, const string baseUrl, const string& contentDirectory, const URI& uri);

	virtual ~Content() {};

	/**
	 *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
	 *  that it derives from the ContentItem this part belongs to.
	 * @return
	 */
	const URI getURI() {
		return URI(baseUrl + "/" + id);
	};
	
	
    /**
     * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     *
     * @param type
     */
	void setType(const string type);

    /**
     * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     */
    string getType();

	/**
	 * Return a new output stream for writing to the content. Any existing content will be overwritten.
	 * @return
	 */
	std::ostream* getOutputStream();

	/**
	 *  Return a new input stream for reading the content.
	 * @return
	 */
	std::istream* getInputStream();
};


inline bool operator==(Content& c1, Content& c2)
{
	return c1.baseUrl == c2.baseUrl && c1.id == c2.id;
}
}
}
#endif
