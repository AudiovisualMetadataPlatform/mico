#ifndef HAVE_SPARQL_UTIL_H
#define HAVE_SPARQL_UTIL_H 1

#include <string>
#include <map>

namespace mico {
  namespace util {

    /**
     * Format a query string with named parameters of the form $(...).
     */
    std::string sparql_format_query(std::string fmt, std::map<std::string,std::string>& parameters);    
    
  }
}

#endif
