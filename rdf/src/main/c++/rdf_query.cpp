#include <cstring>
#include <expat.h>
#include "rdf_query.hpp"

#define BUFFSIZE	8192




namespace org {
  namespace openrdf {
    namespace query {


      /**
       * Serialize query result data from the given argument into XML SPARQL protocol syntax.
       */
      std::ostream& operator<<(std::ostream& os, QueryResult& r) {
	os << "<?xml version=\"1.0\"?>\n";
	os << "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n";
	  
	os << "  <head>\n";
	for(auto var : r.getBindingNames()) {
	  os << "    <variable name=\"" << var << "\"/>\n";
	}
	os << "  </head>\n";

	os << "  <results>\n";
	for(auto bs : r) {
	  os << "    <result>\n";
	  for(auto b : bs) {
	    os << "      <binding name=\"" << b.first << "\">\n";

	    if(b.second->getType() == TYPE_URI) {
	      URI* uri = dynamic_cast<URI*>(b.second);
	      os << "        <uri>" << uri->stringValue() << "</uri>\n";
	    } else if(b.second->getType() == TYPE_BNODE) {
	      BNode* bnode = dynamic_cast<BNode*>(b.second);
	      os << "        <bnode>" << bnode->stringValue() << "</bnode>\n";
	    } else if(b.second->getType() == TYPE_PLAIN_LITERAL) {
	      Literal* literal = dynamic_cast<Literal*>(b.second);
	      os << "        <literal>" << literal->stringValue() << "</literal>\n";
	    } else if(b.second->getType() == TYPE_LANGUAGE_LITERAL) {
	      LanguageLiteral* literal = dynamic_cast<LanguageLiteral*>(b.second);
	      os << "        <literal xml:lang=\"" << literal->getLanguage() << "\">" << literal->stringValue() << "</literal>\n";
	    } else if(b.second->getType() == TYPE_TYPED_LITERAL) {
	      DatatypeLiteral* literal = dynamic_cast<DatatypeLiteral*>(b.second);
	      os << "        <literal datatype=\"" << literal->getDatatype().stringValue() << "\">" << literal->stringValue() << "</literal>\n";
	    }

	    os << "      </binding>\n";
	  }
	  os << "    </result>\n";
	}
	os << "  </results>\n";


	os << "</sparql>\n";
	return os;
      }



      enum ParserMode {
	INIT, HEAD, RESULTS, RESULT, BINDING, MODE_URI, MODE_LITERAL_PLAIN, MODE_LITERAL_TYPED, MODE_LITERAL_LANG, MODE_BNODE
      };

      struct ParserData {
	QueryResult& result;  // the result we are currently building up
	ParserMode   mode;    // the current mode of the parser
	string       name;    // multi-purpose use storage
	string       attr;    // multi-purpose use storage
	void*        data;    // multi-purpose use storage
      };

      static void startElement(void *data, const char *el, const char **attr) {
	ParserData *r = (ParserData*)data;


	switch(r->mode) {
	case INIT:
	  if(strncmp("head",el,4) == 0) {
	    r->mode = HEAD;
	  } else if(strncmp("results",el,7) == 0) {
	    r->mode = RESULTS;
	  } 
	  break;
	case HEAD:
	  if(strncmp("variable",el,8) == 0) {
	    for(int i=0; attr[i]; i += 2) {
	      if(strncmp("name",attr[i],4) == 0) {
		r->result.bindingNames.push_back(std::string(attr[i+1]));
	      }
	    }
	  }
	  break;
	case RESULTS:
	  if(strncmp("result",el,6) == 0) {
	    r->mode = RESULT;
	    r->result.push_back(BindingSet());
	  }
	  break;
	case RESULT:
	  if(strncmp("binding",el,7) == 0) {
	    r->mode = BINDING;
	    r->data = NULL;
	    for(int i=0; attr[i]; i += 2) {
	      if(strncmp("name",attr[i],4) == 0) {
		r->name = attr[i+1];
		break;
	      }
	    }
	  }
	  break;
	case BINDING:
	  if(strncmp("uri",el,3) == 0) {
	    r->mode = MODE_URI;
	  } else if(strncmp("bnode",el,5) == 0) {
	    r->mode = MODE_BNODE;
	  } else if(strncmp("literal",el,7) == 0) {	    
	    r->mode = MODE_LITERAL_PLAIN;

	    // check if the literal is typed or with language
	    for(int i=0; attr[i]; i += 2) {
	      if(strncmp("xml:lang",attr[i],8) == 0) {
		r->mode = MODE_LITERAL_LANG;
		r->attr = attr[i+1];
		break;
	      } else if(strncmp("datatype",attr[i],8) == 0) {
		r->mode = MODE_LITERAL_TYPED;
		r->attr = attr[i+1];
		break;
	      }
	    }
	  }
	  break;
	  
	}
      }


      static void endElement(void *data, const char *el) {
	ParserData *r = (ParserData*)data;

	switch(r->mode) {
	case HEAD:
	  if(strncmp("head",el,4) == 0) {
	    r->mode = INIT;
	  } else if(strncmp("variable",el,8) == 0) {
	    // do nothing, keep mode
	  } else {
	    std::cerr << "invalid close element tag (expected </head>): "<<el<<"\n";
	  }
	  break;
	case RESULTS:
	  if(strncmp("results",el,7) == 0) {
	    r->mode = INIT;
	  } else {
	    std::cerr << "invalid close element tag (expected </results>): "<<el<<"\n";
	  }
	  break;
	case RESULT:
	  if(strncmp("result",el,6) == 0) {
	    r->mode = RESULTS;
	  } else {
	    std::cerr << "invalid close element tag (expected </result>): "<<el<<"\n";
	  }
	  break;
	case BINDING:
	  if(strncmp("binding",el,7) == 0) {
	    r->mode = RESULT;
	  } else {
	    std::cerr << "invalid close element tag (expected </binding>): "<<el<<"\n";
	  }
	  break;
	case MODE_URI:
	  if(strncmp("uri",el,3) == 0) {
	    r->mode = BINDING;
	    r->result.back()[r->name]=static_cast<URI*>(r->data);
	  } else {
	    std::cerr << "invalid close element tag (expected </uri>): "<<el<<"\n";
	  }
	  break;
	case MODE_LITERAL_PLAIN:
	  if(strncmp("literal",el,7) == 0) {
	    r->mode = BINDING;
	    r->result.back()[r->name]=static_cast<Literal*>(r->data);
	  } else {
	    std::cerr << "invalid close element tag (expected </literal>): "<<el<<"\n";
	  }
	  break;
	case MODE_LITERAL_TYPED:
	  if(strncmp("literal",el,7) == 0) {
	    r->mode = BINDING;
	    r->result.back()[r->name]=static_cast<DatatypeLiteral*>(r->data);
	  } else {
	    std::cerr << "invalid close element tag (expected </literal>): "<<el<<"\n";
	  }
	  break;
	case MODE_LITERAL_LANG:
	  if(strncmp("literal",el,7) == 0) {
	    r->mode = BINDING;
	    r->result.back()[r->name]=static_cast<LanguageLiteral*>(r->data);
	  } else {
	    std::cerr << "invalid close element tag (expected </literal>): "<<el<<"\n";
	  }
	  break;
	case MODE_BNODE:
	  if(strncmp("bnode",el,5) == 0) {
	    r->mode = BINDING;
	    r->result.back()[r->name]=static_cast<BNode*>(r->data);
	  } else {
	    std::cerr << "invalid close element tag (expected </bnode>): "<<el<<"\n";
	  }
	  break;
	}
      }


      static void characterData(void *data, const char *chars, int len) {
	ParserData *r = (ParserData*)data;
	switch(r->mode) {
	case MODE_URI:
	  r->data = new URI(std::string(chars,len));
	  break;	 
	case MODE_BNODE:
	  r->data = new BNode(std::string(chars,len));
	  break;	 
	case MODE_LITERAL_PLAIN:
	  r->data = new Literal(std::string(chars,len));
	  break;	 
	case MODE_LITERAL_TYPED:
	  r->data = new DatatypeLiteral(std::string(chars,len), r->attr);
	  break;	 
	case MODE_LITERAL_LANG:
	  r->data = new LanguageLiteral(std::string(chars,len), r->attr);
	  break;	 
	}
      }


      /**
       * Load query result data represented in the XML SPARQL protocol syntax into the query result
       * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
       */
      std::istream& operator>>(std::istream& is, QueryResult& r) {
	ParserData data = { r, INIT };

	XML_Parser p = XML_ParserCreate("UTF-8");
	XML_SetElementHandler(p, startElement, endElement);	
	XML_SetCharacterDataHandler(p, characterData);
	XML_SetUserData(p, &data);

	char buf[BUFFSIZE];	

	while(is.read(buf,BUFFSIZE)) {
	  cout << "parsing "<< is.gcount() << " bytes ...\n";
	  if(! XML_Parse(p,buf,is.gcount(),0) ) {
	    std::cerr << "while parsing SPARQL XML result: parse error at line " << XML_GetCurrentLineNumber(p) << ", " << XML_ErrorString(XML_GetErrorCode(p)) << "\n";
	    break;
	  }
	}
	if(! XML_Parse(p,buf,is.gcount(),1) ) {
	  std::cerr << "while parsing SPARQL XML result: parse error at line " << XML_GetCurrentLineNumber(p) << ", " << XML_ErrorString(XML_GetErrorCode(p)) << "\n";
	}
	
	return is;
      }

      

    }
  }
}
