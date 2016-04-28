#ifndef HAVE_ANALYSIS_SERVICE_H
#define HAVE_ANALYSIS_SERVICE_H 1

#include <string>
#include <list>
#include <map>

#include "rdf_model.hpp"
#include "Item.hpp"

namespace mico {
    namespace event {

        class AnalysisResponse; //class forward declaration
        /**
        * Interface to be implemented by services. Consists of some informational methods as well as a callback which is called
        * whenever a new event for this service has been received.
        *
        * @author Sebastian Schaffert (sschaffert@apache.org)
        */
        class AnalysisService {

        protected:
            mico::rdf::model::URI serviceID;
            std::string provides;
            std::string requires;
            std::string queue;

        public:
            AnalysisService(const std::string serviceID, const std::string requires, const std::string provides, const std::string queue)
                    : serviceID(serviceID), provides(provides), requires(requires), queue(queue) {};


            virtual ~AnalysisService() {};

            /**
            * Return a unique ID (URI) that identifies this service and its functionality.
            *
            * @return a unique ID identifying this service globally
            */
            virtual const mico::rdf::model::URI& getServiceID() const { return serviceID; };


            /**
            * Return the type of output produced by this service as symbolic identifier. In the first version of the API, this
            * is simply an arbitrary string (e.g. MIME type)
            *
            * @return a symbolic identifier representing the output type of this service
            */
            virtual const std::string& getProvides() const { return provides; };


            /**
            * Return the type of input required by this service as symbolic identifier. In the first version of the API, this
            * is simply an arbitrary string (e.g. MIME type)
            *
            * @return  a symbolic identifier representing the input type of this service
            */
            virtual const std::string& getRequires() const { return requires; };


            /**
            * Return the queue name that should be used by the messaging infrastructure for this service. If explicitly set,
            * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
            * balancing.
            *
            * The implementation can return null, in which case the event API will choose a random queue name.
            *
            * @return a string identifying the queue name this service wants to use
            */
            virtual const std::string& getQueueName() const { return queue; };


            /**
            * Call this service for the given content item and object. This method is called by the event manager whenever
            * a new analysis event for this service has been received in its queue. The API takes care of automatically
            * resolving the content item in the persistence service.
            *
            * @param resp   a response object that can be used to send back notifications about new objects to the broker
            * @param ci     the content item to analyse
            * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
            */
            virtual void call(mico::event::AnalysisResponse& response, std::shared_ptr< mico::persistence::model::Item > item, std::list<mico::rdf::model::URI>& object, std::map<std::string,std::string>& params) = 0;
        };

    }
}
#endif
