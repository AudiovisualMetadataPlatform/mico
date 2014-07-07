#ifndef HAVE_CONTENT_H
#define HAVE_CONTENT_H 1

#include <iostream>
#include "../../../../rdf/src/main/c++/rdf_model.hpp"


namespace mico {
  namespace persistence {

    using namespace mico::rdf::model;

    class Content {

    public:
      /**
       *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
       *  that it derives from the ContentItem this part belongs to.
       * @return
       */
      URI getURI();

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
