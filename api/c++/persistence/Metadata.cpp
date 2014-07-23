#include "Metadata.hpp"

namespace mico {
  namespace persistence {

    /**
     * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
     *
     * @param in      InputStream to load the data from
     * @param format  data format the RDF data is using (e.g. Turtle)
     */
    void Metadata::load(std::istream& in, const string format) {
      
      std::stringstream buffer;
      buffer << in.rdbuf();

      if(buffer.str() != "") {

	Request req(POST,baseUrl+"/import/upload?context="+contextUrl);

	if(format.find("/") != std::string::npos) {
	  req.setBody(buffer.str(),format);
	} else if(format == "turtle" || format == "ttl") {
	  req.setBody(buffer.str(),"text/turtle");
	} else if(format == "rdfxml" || format == "xml" || format == "rdf") {
	  req.setBody(buffer.str(),"application/rdf+xml");
	}
  
	Response* resp = httpClient.execute(req);
	delete resp;
      } else {
	std::cerr << "error loading data: input stream was empty" << std::endl;
      }
    }


    /**
     * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
     * format. Can be used for exporting the metadata.
     *
     * @param out    OutputStream to export the data to
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    void Metadata::dump(std::ostream& out, const string format) {
      string _format;
      if(format.find("/") != std::string::npos) {
	_format = format;
      } else if(format == "turtle" || format == "ttl") {
	_format = "text/turtle";
      } else if(format == "rdfxml" || format == "xml" || format == "rdf") {
	_format = "application/rdf+xml";
      }
      
      Request req(GET,baseUrl+"/export/download?context="+contextUrl+"&format="+_format);

      Response* resp = httpClient.execute(req);

      if(resp->getStatus() >= 200 && resp->getStatus() < 300) {
	const Body* b = resp->getBody();
	out.write(b->getContent(),b->getContentLength());
      }

      delete resp;
    }



    /**
     * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
     * used for any kind of modification of the data.
     *
     * @param sparqlUpdate
     */
    void Metadata::update(const string sparqlUpdate) {
      sparqlClient.update(sparqlUpdate);
    }


    /**
     * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    const TupleResult* Metadata::query(const string sparqlQuery) {
      return sparqlClient.query(sparqlQuery);
    }



    /**
     * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    const bool Metadata::ask(const string sparqlQuery) {
      return sparqlClient.ask(sparqlQuery);
    }




  }
}
