#include "Uri.hpp"

namespace mico {
namespace persistence {
namespace model {

using namespace std;

size_t URI::split() const {
    size_t found = uri.find('#');
    if(found != string::npos) {
        return found + 1;
    }

    found = uri.rfind('/');
    if(found != string::npos) {
        return found + 1;
    }

    found = uri.rfind(':');
    if(found != string::npos) {
        return found + 1;
    }

    return string::npos;
}

}}}
