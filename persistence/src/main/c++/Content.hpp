#ifndef HAVE_CONTENT_H
#define HAVE_CONTENT_H 1

#include <string>
#include <iostream>
#include "rdf_model.hpp"


namespace mico {
  namespace persistence {

    using std::string;
    using namespace mico::rdf::model;   

    class Content {

    protected:
      string baseUrl;
      string id;

    public:
      Content(const string baseUrl, const string id) : baseUrl(baseUrl), id(id) {};

      Content(const string baseUrl, const URI& uri);

      virtual ~Content() {};

      /**
       *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
       *  that it derives from the ContentItem this part belongs to.
       * @return
       */
      const URI getURI() { return URI(baseUrl + "/" + id); };

      /**
       * Return a new output stream for writing to the content. Any existing content will be overwritten.
       * @return
       */
      std::ostream& getOutputStream();

      /**
       *  Return a new input stream for reading the content.
       * @return
       */
      std::istream& getInputStream();
    };
  }
}
#endif
