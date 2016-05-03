#ifndef ASSET_HPP
#define ASSET_HPP 1

#include <string>
#include <iostream>

namespace mico {

    namespace persistence  {
    namespace model  {

      class URI;

      class Asset
      {
        public:
          virtual mico::persistence::model::URI getLocation() = 0;

          virtual std::string getFormat() = 0;

          virtual void setFormat(std::string format) = 0;

          /**
           * Return a new output stream for writing to the content. Any existing content will be overwritten.
           *
           * @return
           */
          virtual std::ostream* getOutputStream() = 0;

          /**
           * Return a new input stream for reading the content.
           *
           * @return
           */
          virtual std::istream* getInputStream() = 0;
      };
    }
  }
}
#endif
