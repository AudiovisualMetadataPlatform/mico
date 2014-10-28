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
