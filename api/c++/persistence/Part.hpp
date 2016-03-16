#ifndef PART_HPP
#define PART_HPP 1

#include <list>

#include "Resource.hpp"

namespace mico {
  namespace persistence {

  class Item;

    /**
     * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
     * content.
     */
    class Part: public Resource
    {
      public:
        /**
         * Return the parent content item.
         * @return
         */
        virtual const Item& getItem() = 0;

        virtual jnipp::LocalRef<ComGithubAnno4jModelBody> getBody() = 0;

        virtual void setBody(const jnipp::LocalRef<ComGithubAnno4jModelBody> &body) = 0;

        //virtual std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > getTargets() = 0;

        virtual void setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets) = 0;

        virtual void addTarget(const jnipp::LocalRef<ComGithubAnno4jModelTarget> &target) = 0;

        //virtual std::list<Resource> getInputs() = 0;

        virtual void setInputs(std::list<Resource> inputs) = 0;

        virtual void addInput(Resource& input) = 0;

        virtual std::string getSerializedAt() = 0;

        jnipp::LocalRef<ComGithubAnno4jModelAgent> getSerializedBy();
    };
  }
}
#endif
