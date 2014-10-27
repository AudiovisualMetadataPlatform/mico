#include <cstring>
#include <expat.h>
#include "rdf_query.hpp"

#define BUFFSIZE	8192


using namespace std;

namespace mico {
    namespace rdf {
        namespace query {


            /**
            * Serialize query result data from the given argument into XML SPARQL protocol syntax.
            */
            std::ostream& operator<<(std::ostream& os, BooleanResult& r) {
                os << "<?xml version=\"1.0\"?>\n";
                os << "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n";

                os << "  <boolean>";
                if(r) {
                    os << "true";
                } else {
                    os << "false";
                }
                os << "</boolean>\n";

                os << "</sparql>\n";
                return os;
            }



            typedef enum {
                INIT, MODE_BOOLEAN
            } ParserMode;

            struct ParserData {
                BooleanResult& result;  // the result we are currently building up
                ParserMode   mode;    // the current mode of the parser
            };

            void BooleanResult::startElement(void *data, const char *el, const char **attr) {
                ParserData *r = (ParserData*)data;

                switch(r->mode) {
                    case INIT:
                        if(strncmp("boolean",el,7) == 0) {
                            r->mode = MODE_BOOLEAN;
                        }
                        break;
                    default:
                        // do nothing
                        break;
                }
            }


            void BooleanResult::endElement(void *data, const char *el) {
                ParserData *r = (ParserData*)data;

                switch(r->mode) {
                    case MODE_BOOLEAN:
                        if(strncmp("boolean",el,7) == 0) {
                            r->mode = INIT;
                        } else {
                            std::cerr << "invalid close element tag (expected </head>): "<<el<<"\n";
                        }
                        break;
                    default:
                        // do nothing
                        break;
                }
            }


            void BooleanResult::characterData(void *data, const char *chars, int len) {
                ParserData *r = (ParserData*)data;
                switch(r->mode) {
                    case MODE_BOOLEAN:
                        if(strncasecmp("true",(const char*)chars,len) == 0) {
                            r->result.value = true;
                        } else {
                            r->result.value = false;
                        }
                        break;
                    default:
                        // do nothing
                        break;
                }
            }



            /**
            * Load query result data represented in the XML SPARQL protocol syntax into the query result
            * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
            */
            void BooleanResult::loadFrom(istream& is) {
                ParserData data = { *this, INIT };

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
            }


            /**
            * Load query result data represented in the XML SPARQL protocol syntax into the query result
            * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
            */
            void BooleanResult::loadFrom(const char* ptr, size_t len) {
                ParserData data = { *this, INIT };

                XML_Parser p = XML_ParserCreate("UTF-8");
                XML_SetElementHandler(p, BooleanResult::startElement, BooleanResult::endElement);
                XML_SetCharacterDataHandler(p, BooleanResult::characterData);
                XML_SetUserData(p, &data);

                if(! XML_Parse(p,ptr,len,1) ) {
                    std::cerr << "while parsing SPARQL XML result: parse error at line " << XML_GetCurrentLineNumber(p) << ", " << XML_ErrorString(XML_GetErrorCode(p)) << "\n";
                }
            }


            /**
            * Load query result data represented in the XML SPARQL protocol syntax into the query result
            * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
            */
            std::istream& operator>>(std::istream& is, BooleanResult& r) {
                r.loadFrom(is);
                return is;
            }


        }
    }
}
