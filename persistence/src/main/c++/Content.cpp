#include <boost/algorithm/string.hpp>

#include "Content.hpp"

using namespace std;
using namespace boost;

namespace mico {
  namespace persistence {

    Content::Content(const string baseUrl, const URI& uri) : baseUrl(baseUrl) {
      if(!starts_with(uri.stringValue(),baseUrl)) {
	throw string("the baseUrl is not a prefix of the URI, invalid argument");
      }

      id = uri.stringValue().substr(baseUrl.length() + 1);
    }


    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     * @return
     */
    std::ostream& Content::getOutputStream() {
      throw string("operation not yet supported!");
    }

    /**
     *  Return a new input stream for reading the content.
     * @return
     */
    std::istream& Content::getInputStream() {
      throw string("operation not yet supported!");
    }

  }
}
