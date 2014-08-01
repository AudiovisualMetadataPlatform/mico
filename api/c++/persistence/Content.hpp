#ifndef HAVE_CONTENT_H
#define HAVE_CONTENT_H 1

#include <string>
#include <iostream>


namespace mico {
  namespace rdf {
    namespace model {
      class URI;
      class Value;
    }
  }

  namespace persistence  {

    class ContentItem;

    class Content
    {
      friend bool operator==(Content& c1, Content& c2);

    protected:
      ContentItem& item;
      const std::string baseUrl;
      std::string id;
      const std::string& contentDirectory;

    public:
      Content(ContentItem& item, const std::string baseUrl, const std::string& contentDirectory, const std::string id) : item(item), baseUrl(baseUrl), id(id), contentDirectory(contentDirectory) {};

      Content(ContentItem& item, const std::string baseUrl, const std::string& contentDirectory, const mico::rdf::model::URI& uri);

      virtual ~Content() {};

      /**
       *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
       *  that it derives from the ContentItem this part belongs to.
       * @return
       */
      const mico::rdf::model::URI getURI();
	
	
      /**
       * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
       * representation). Ideally, the type comes from a controlled vocabulary.
       *
       * @param type
       */
      void setType(const std::string type);

      /**
       * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
       * representation). Ideally, the type comes from a controlled vocabulary.
       */
      std::string getType();


      /**
       * Set the property with the given URI to the given value. Use e.g. in combination with fixed vocabularies.
       */
      void setProperty(const mico::rdf::model::URI& property, const std::string value);


      /**
       * Return the property value of this content part for the given property. Use e.g. in combination with fixed vocabularies.
       */
      std::string getProperty(const mico::rdf::model::URI& property);


      /**
       * Set the property with the given URI to the given value. Use e.g. in combination with fixed vocabularies.
       */
      void setRelation(const mico::rdf::model::URI& property, const mico::rdf::model::URI& value);

      /**
       * Return the property value of this content part for the given property. Use e.g. in combination with fixed vocabularies.
       */
      mico::rdf::model::Value* getRelation(const mico::rdf::model::URI& property);

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
