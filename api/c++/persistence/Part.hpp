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

      /**
       * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
       * content.
       */
      class Part
      {
        public:
          /**
           * Return the parent content item.
           * @return
           */
          virtual std::shared_ptr<Item> getItem() = 0;

          virtual jnipp::LocalRef<ComGithubAnno4jModelBody> getBody() = 0;

          virtual void setBody(const jnipp::LocalRef<ComGithubAnno4jModelBody> &body) = 0;

          virtual std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > getTargets() = 0;

          virtual void setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets) = 0;

          virtual void addTarget(const jnipp::LocalRef<ComGithubAnno4jModelTarget> &target) = 0;

          virtual std::list< std::shared_ptr<Resource> > getInputs() = 0;

          virtual void setInputs(std::list< std::shared_ptr<Resource> > inputs) = 0;

          virtual void addInput(Resource& input) = 0;

          virtual std::string getSerializedAt() = 0;

          jnipp::LocalRef<ComGithubAnno4jModelAgent> getSerializedBy();
      };
    }
  }
}
#endif
