#ifndef ITEM_HPP
#define ITEM_HPP 1

#include "Part.hpp"
#include "anno4cpp.h"

namespace mico {
  namespace persistence {
    namespace model {
      class URI;

      /**
       * Representation of a Item. A Item is a collection of ContentParts, e.g. an HTML page together with
       * its embedded images. ContentParts can be either original content or created during analysis. For compatibility
       * with the Linked Data platform, its RDF type is ldp:BasicContainer
       */
      class Item
      {
        public:
          /**
           * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
           * content and metadata of the content part.
           *
           * @param extractorID The id of the extractor which creates the current part
           * @return a handle to a ContentPart object that is suitable for reading and updating
           */
          virtual std::shared_ptr<Part> createPart(const mico::persistence::model::URI& extractorID) = 0;

          /**
           * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
           * content part.
           *
           * @param uri the URI of the content part to return
           * @return a handle to a ContentPart object that is suitable for reading and updating
           */
          virtual std::shared_ptr<Part> getPart(const mico::persistence::model::URI& uri) = 0;

          /**
           * Return a list over all content parts contained in this item.
           *
           * @return a list that holds the content parts
           */
          virtual std::list< std::shared_ptr<Part> > getParts() = 0;

          /**
           * Create a new jnipp Object of the desired class.
           */
          virtual jnipp::Ref<JavaLangObject>& createObject(const jnipp::Ref<jnipp::Class>& clazz) = 0;

          /**
           * Retrieve an existing jnipp Object of the desired class.
           */
          virtual jnipp::Ref<JavaLangObject>& findObject(const  mico::persistence::model::URI& uri, const jnipp::Ref<jnipp::Class>& clazz) = 0;


          virtual std::string getSerializedAt() = 0;

      };
    }
  }
}
#endif
