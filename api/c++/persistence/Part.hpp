#ifndef PART_HPP
#define PART_HPP 1

#include "Resource.hpp"

#include <list>
#include <memory>
#include <jnipp.h>
#include <anno4cpp.h>


namespace mico {
  namespace persistence {
    namespace model {

      class Item;


      class Part
      {
        public:
          /**
           * Return the parent content item.
           * @return
           */
          virtual std::shared_ptr<Item> getItem() = 0;

          virtual jnipp::LocalRef<jnipp::com::github::anno4j::model::Body> getBody() = 0;

          virtual void setBody(const jnipp::LocalRef<jnipp::com::github::anno4j::model::Body> &body) = 0;

          virtual std::list< jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> > getTargets() = 0;

          virtual void setTargets(std::list< jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> > targets) = 0;

          virtual void addTarget(const jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> &target) = 0;

          virtual std::list< std::shared_ptr<Resource> > getInputs() = 0;

          virtual void setInputs(std::list< std::shared_ptr<Resource> > inputs) = 0;

          virtual void addInput(Resource& input) = 0;

          virtual std::string getSerializedAt() = 0;

          jnipp::LocalRef<jnipp::com::github::anno4j::model::Agent> getSerializedBy();
      };
    }
  }
}
#endif
