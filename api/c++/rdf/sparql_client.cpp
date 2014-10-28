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
#include "sparql_client.hpp"

using namespace mico::http;

namespace mico {
    namespace rdf {
        namespace query {


            /**
            * Execute a SPARQL 1.1 ask query against the Marmotta server.
            */
            const bool SPARQLClient::ask(std::string sparqlAsk) {
                Request req(POST,base_url+"/select");
                req.setHeader("Accept","application/sparql-results+xml");
                req.setBody(sparqlAsk, "application/sparql-query");

                Response *resp = http_client.execute(req);

                if(resp->getStatus() == 200 && resp->getBody() != NULL) {
                    BooleanResult *r = new BooleanResult();
                    r->loadFrom(resp->getBody()->getContent(), resp->getBody()->getContentLength());
                    return (bool)*r;
                } else {
                    throw QueryFailedException(resp->getStatus(), "HTTP response without body");
                }

            }

            /**
            * Execute a SPARQL 1.1 tuple query against the Marmotta server.
            */
            const TupleResult* SPARQLClient::query(std::string sparqlSelect) {
                Request req(POST,base_url+"/select");
                req.setHeader("Accept","application/sparql-results+xml");
                req.setBody(sparqlSelect, "application/sparql-query");

                Response *resp = http_client.execute(req);

                if(resp->getStatus() == 200 && resp->getBody() != NULL) {
                    TupleResult *r = new TupleResult();
                    r->loadFrom(resp->getBody()->getContent(), resp->getBody()->getContentLength());
                    return r;
                } else {
                    throw QueryFailedException(resp->getStatus(), "HTTP response without body");
                }
            }


            /**
            * Execute a SPARQL 1.1 Update against the Marmotta server
            */
            const void SPARQLClient::update(std::string sparqlUpdate) {
                Request req(POST,base_url+"/update");
                req.setBody(sparqlUpdate, "application/sparql-update");

                Response *resp = http_client.execute(req);

                if(resp->getStatus() != 200) {
                    throw QueryFailedException(resp->getStatus(), "SPARQL update failed");
                }
            }

        }
    }
}
