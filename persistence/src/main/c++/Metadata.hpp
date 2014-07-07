#ifndef HAVE_METADATA_H
#define HAVE_METADATA_H 1

#include <string>
#include <iostream>
#include "../../../../rdf/src/main/c++/rdf_model.hpp"
#include "../../../../rdf/src/main/c++/rdf_query.hpp"


namespace mico {
  namespace persistence {

    using std::string;
    using namespace mico::rdf::model;
    using namespace mico::rdf::query;

    class Metadata {

    public:
      /**
       * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
       *
       * @param in      InputStream to load the data from
       * @param format  data format the RDF data is using (e.g. Turtle)
       */
      void load(std::istream& in, const string format);


      /**
       * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
       * format. Can be used for exporting the metadata.
       *
       * @param out    OutputStream to export the data to
       * @param format data format the RDF data is using (e.g. Turtle)
       */
      void dump(std::ostream& out, const string format);



      /**
       * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
       * used for any kind of modification of the data.
       *
       * @param sparqlUpdate
       */
      void update(const string sparqlUpdate);


      /**
       * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      const TupleResult* query(const string sparqlQuery);



      /**
       * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      bool ask(const string sparqlQuery);


      /**
       * Close the metadata connection and clean up any open resources.
       */
      void close();

    };
  }
}

#endif
