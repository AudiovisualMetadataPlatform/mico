#ifndef HAVE_DUBLINCORE_HPP
#define HAVE_DUBLINCORE_HPP 1

#include <utime.h>
#include "rdf_model.hpp"

namespace mico {
    namespace persistence {

        class DublinCore {

            using mico::rdf::model::URI;
            using std::string;

        private:

            string title;
            string description;
            string type;

            URI creator;
            URI source;

            time_t created;

        };

    }
}


#endif