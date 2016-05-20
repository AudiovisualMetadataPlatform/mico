#ifndef URI_HPP
#define URI_HPP

#include <string>
#include <sstream>

namespace mico {
namespace persistence {
namespace model {

/**
* A URI. A URI consists of a namespace and a local name, which are derived from a URI string
* by splitting it in two using the following algorithm:
* - Split after the first occurrence of the '#' character,
* - If this fails, split after the last occurrence of the '/' character,
* - If this fails, split after the last occurrence of the ':' character.
*
* The last step should never fail as every legal (full) URI contains at least one ':'
* character to seperate the scheme from the rest of the URI. The implementation should check
* this upon object creation.
*/
class URI {

private:
    std::string uri;

    // find split position according to algorithm in class description
    std::size_t split() const;

    std::ostream& print(std::ostream& os) const { os << "URI("<<uri<<")"; return os; }

public:

    URI(const std::string& uri) : uri(uri) {}
    URI(const char* uri)   : uri(uri) {}
    URI(const mico::persistence::model::URI& uri)    : uri(uri.uri) {}

    /**
    * Gets the local name of this URI.
    */
    std::string getLocalName() const { return uri.substr(split()); }

    /**
    * Gets the namespace of this URI.
    */
    std::string getNamespace() const { return uri.substr(0,split()); }


    /**
    * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
    */
    inline const std::string& stringValue() const { return uri; }


    inline bool operator==(const mico::persistence::model::URI& u) const { return uri == u.uri; }
    inline bool operator==(const std::string& s) const { return uri == s; }
    inline bool operator==(const char* s) const { return uri == s; }
    inline bool operator!=(const std::string& s) const { return uri != s; }
    inline bool operator!=(const char* s) const { return uri != s; }
};


}}}

#endif // URI_HPP
