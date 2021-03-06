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
#include <boost/uuid/uuid_generators.hpp>
#include <boost/algorithm/string.hpp>

#include <map>
#include <string>

#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"
#include "rdf_model.hpp"
#include "rdf_query.hpp"

using namespace std;
using namespace boost;
using namespace uuids;
using namespace mico::util;
using namespace mico::rdf::model;
using namespace mico::rdf::query;

// extern references to constant SPARQL templates
SPARQL_INCLUDE(askContentPart);
SPARQL_INCLUDE(createContentPart);
SPARQL_INCLUDE(deleteContentPart);
SPARQL_INCLUDE(listContentParts);

// UUID generators
static random_generator rnd_gen;
static string_generator str_gen;

namespace mico
{
    namespace persistence
    {


        ContentItem::ContentItem(const string& baseUrl, const string& contentDirectory, const uuid& id)
                : baseUrl(baseUrl), contentDirectory(contentDirectory), id(id)
                , metadata(baseUrl, boost::uuids::to_string(id) + SUFFIX_METADATA)
                , execution(baseUrl, boost::uuids::to_string(id) + SUFFIX_EXECUTION)
                , result(baseUrl, boost::uuids::to_string(id) + SUFFIX_RESULT)
        { };


        ContentItem::ContentItem(const string& baseUrl, const string& contentDirectory, const URI& uri)
                : baseUrl(baseUrl), contentDirectory(contentDirectory), id(str_gen(uri.stringValue().substr(baseUrl.length() + 1)))
                , metadata(baseUrl, uri.stringValue().substr(baseUrl.length() + 1) + SUFFIX_METADATA)
                , execution(baseUrl, uri.stringValue().substr(baseUrl.length() + 1) + SUFFIX_EXECUTION)
                , result(baseUrl, uri.stringValue().substr(baseUrl.length() + 1) + SUFFIX_RESULT)
        {
            if(!starts_with(uri.stringValue(),baseUrl)) {
                throw string("the baseUrl is not a prefix of the URI, invalid argument");
            }

        }



        /**
        * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
        * content and metadata of the content part.
        *
        * @return a handle to a ContentPart object that is suitable for reading and updating
        */
        Content* ContentItem::createContentPart()
        {
            uuid contentUUID = rnd_gen();

            Content* content = new Content(*this,baseUrl, contentDirectory, boost::uuids::to_string(id) + "/" + boost::uuids::to_string(contentUUID));

            map<string,string> params;
            params["ci"] = baseUrl + "/" + boost::uuids::to_string(id);
            params["cp"] = baseUrl + "/" + boost::uuids::to_string(id) + "/" + boost::uuids::to_string(contentUUID);

            metadata.update(SPARQL_FORMAT(createContentPart, params));

            return content;
        }

        /**
        * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
        * content and metadata of the content part.
        *
        * @param id the URI of the content part to create
        * @return a handle to a ContentPart object that is suitable for reading and updating
        */
        Content* ContentItem::createContentPart(const URI& id)
        {
            Content* content = new Content(*this,baseUrl,contentDirectory, id);

            map<string,string> params;
            params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
            params["cp"] = id.stringValue();

            metadata.update(SPARQL_FORMAT(createContentPart, params));

            return content;
        }

        /**
        * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
        * content part.
        *
        * @param id the URI of the content part to return
        * @return a handle to a ContentPart object that is suitable for reading and updating
        */
        Content* ContentItem::getContentPart(const URI& id)
        {
            map<string,string> params;
            params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
            params["cp"] = id.stringValue();

            if(metadata.ask(SPARQL_FORMAT(askContentPart,params))) {
                return new Content(*this,baseUrl,contentDirectory, id);
            } else {
                return NULL;
            }
        }


        /**
        * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
        * nothing.
        *
        * @param id the URI of the content part to delete
        */
        void ContentItem::deleteContentPart(const URI& id)
        {
            // delete any possible binary content
            Content* c = getContentPart(id);
            c->deleteContent();
            delete c;

            // delete metadata
            map<string,string> params;
            params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
            params["cp"] = id.stringValue();

            metadata.update(SPARQL_FORMAT(deleteContentPart, params));
        }


        /**
        * Convenient C++ style operator for accessing and constructing content parts. Returns
        * the content part with the given ID if found or a newly created content part if not found.
        */
        Content* ContentItem::operator[](const URI& id)
        {
            Content* r = getContentPart(id);
            if(!r) {
                r = createContentPart(id);
            }
            return r;
        }


        /**
        * Return an iterator over all content parts contained in this content item.
        *
        * @return an iterable that (lazily) iterates over the content parts
        */
        content_part_iterator ContentItem::begin()
        {
            map<string,string> params;
            params["ci"] = baseUrl + "/" + boost::uuids::to_string(id);

            const TupleResult* r = metadata.query(sparql_format_query(sparql_listContentParts,params));
            if(r->size() > 0) {
                return content_part_iterator(*this,baseUrl,contentDirectory,r);
            } else {
                delete r;
                return content_part_iterator(*this,baseUrl,contentDirectory);
            }
        }


        content_part_iterator ContentItem::end()
        {
            return content_part_iterator(*this,baseUrl,contentDirectory);
        };


        void content_part_iterator::increment() {
            pos = pos+1 == result->size() ? -1 : pos + 1;
        };

        bool content_part_iterator::equal(content_part_iterator const& other) const {
            return this->pos == other.pos;
        };

        Content* content_part_iterator::dereference() const {
            return new Content(item, baseUrl, contentDirectory, *dynamic_cast<const URI*>( result->at(pos).at("p") ) );
        }

    }
}
