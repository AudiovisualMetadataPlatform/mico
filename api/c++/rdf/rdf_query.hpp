#ifndef HAVE_RDF_QUERY_H
#define HAVE_RDF_QUERY_H 1

#include <iostream>
#include <map>
#include <vector>
#include <string>



namespace mico {
    namespace rdf {
        namespace model {
            class Value;
        }

        namespace query {

            /**
            * A BindingSet is a single result row of a TupleResult. It maps from variable name to
            * variable binding and is implemented as a simple map.
            */
            typedef std::map<std::string,mico::rdf::model::Value*> BindingSet;


            /**
            * Abstract base class for query results. Defines methods for loading and parsing query results from
            * different sorts of input.
            */
            class QueryResult {
            public:

                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                virtual void loadFrom(std::istream& is) = 0;


                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                virtual void loadFrom(const char* ptr, size_t len) = 0;

            };

            /**
            * The boolean result of a SPARQL ASK query. Can be used like the bool datatype.
            */
            class BooleanResult : public QueryResult {

                static void startElement(void *data, const char *el, const char **attr);
                static void endElement(void *data, const char *el);
                static void characterData(void *data, const char *chars, int len);

            private:
                bool value;

            public:
                BooleanResult() : value(false) {};
                BooleanResult(bool value) : value(value) {};

                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                void loadFrom(std::istream& is);


                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                void loadFrom(const char* ptr, size_t len);


                // can be used as boolean value in boolean expressions...
                inline operator bool() const { return value; };

            };



            /**
            * The result of a SPARQL SELECT query. Implemented as a vector of BindingSet instances, each
            * representing one row in the result.
            */
            class TupleResult : public QueryResult {

            private:
                friend std::istream& operator>>(std::istream& is, TupleResult& r);
                friend std::ostream& operator<<(std::ostream& is, TupleResult& r);

                std::vector<std::string> bindingNames;
                std::vector<BindingSet>  data;

                static void startElement(void *data, const char *el, const char **attr);
                static void endElement(void *data, const char *el);
                static void characterData(void *data, const char *chars, int len);

            public:

                virtual ~TupleResult() {};


                /**
                * Return the binding names (variable names) contained in the result.
                */
                const std::vector<std::string>& getBindingNames() const { return bindingNames; };


                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                void loadFrom(std::istream& is);


                /**
                * Load query result data represented in the XML SPARQL protocol syntax into the query result
                * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
                */
                void loadFrom(const char* ptr, size_t len);


                // delegate methods
                inline std::vector<BindingSet>::iterator begin() { return data.begin(); }
                inline std::vector<BindingSet>::iterator end() { return data.end(); }
                inline size_t size() const { return data.size(); }
                inline BindingSet& at(size_t i) { return data.at(i); }
                inline const BindingSet& at(size_t i) const { return data.at(i); }

                inline BindingSet& operator[](size_t i) { return data[i]; };
                inline const BindingSet& operator[](size_t i) const { return data[i]; };
            };




            /**
            * Load query result data represented in the XML SPARQL protocol syntax into the query result
            * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
            */
            std::istream& operator>>(std::istream& is, TupleResult& r);
            std::istream& operator>>(std::istream& is, BooleanResult& r);


            /**
            * Serialize query result data from the given argument into XML SPARQL protocol syntax.
            */
            std::ostream& operator<<(std::ostream& is, TupleResult& r);
            std::ostream& operator<<(std::ostream& is, BooleanResult& r);


        }
    }
}


#endif
