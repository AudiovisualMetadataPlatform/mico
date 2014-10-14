#ifndef HAVE_SPARQL_UTIL_H
#define HAVE_SPARQL_UTIL_H 1

#include <string>
#include <map>

/**
 * Include an external SPARQL named query with parameters. The query must have been transformed into a C byte array using xxd -i and compiled and linked separately.
 */ 
#define SPARQL_INCLUDE(NAME) \
	extern unsigned char NAME ## _sparql []; \
	extern unsigned int  NAME ## _sparql_len ; \
	const std::string sparql_ ## NAME ((char*) NAME ## _sparql, NAME ## _sparql_len);

#define SPARQL_QUERY(NAME) sparql_ ## NAME

/** 
 * Format a named sparql query with the given parameters. The query needs to be included before with SPARQL_INCLUDE.
 */ 
#define SPARQL_FORMAT(NAME,PARAMS) mico::util::sparql_format_query(SPARQL_QUERY(NAME), PARAMS)

namespace mico {
  namespace util {

    /**
     * Format a query string with named parameters of the form $(...).
     */
    std::string sparql_format_query(std::string fmt, std::map<std::string,std::string>& parameters);    
    
  }
}

#endif
