#include "SPARQLUtil.hpp"

#include <boost/xpressive/xpressive.hpp>
#include <boost/xpressive/regex_actions.hpp>

namespace mico {
  namespace util {

    using namespace boost;
    using namespace xpressive;

    static const sregex var = "$(" >> (s1 = +_w) >> ')';

    std::string sparql_format_query(std::string fmt, std::map<std::string,std::string>& parameters) {
      return regex_replace(fmt, var, boost::xpressive::ref(parameters)[s1]);
    }
  }
}
