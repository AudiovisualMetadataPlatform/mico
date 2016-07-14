#ifndef HAVE_ANALYSIS_SERVICE_H
#define HAVE_ANALYSIS_SERVICE_H 1

#include <string>
#include <list>
#include <map>

#include "rdf_model.hpp"
#include "Item.hpp"
#include "Uri.hpp"


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
            mico::persistence::model::URI serviceID;
            std::string queue;
            std::string extractorID;
            std::string extractorModeID;
            std::string extractorVersion;
            std::string provides;
            std::string requires;

        public:
            AnalysisService(const std::string extractorID, const std::string extractorModeID, const std::string extractorVersion,
            		        const std::string requires, const std::string provides)
                    : serviceID("http://www.mico-project.org/services/" + extractorID + "-" + extractorVersion + "-" + extractorModeID),
					  queue(extractorID + "-" + extractorVersion + "-" + extractorModeID),
					  extractorID(extractorID), extractorModeID(extractorModeID), extractorVersion(extractorVersion),
					  provides(provides), requires(requires) {}


            virtual ~AnalysisService() {}

            /**
            * Return a unique ID (URI) that identifies this service and its functionality.
            *
            * @return a unique ID identifying this service globally
            */
            virtual const mico::persistence::model::URI& getServiceID() const final {
            	return serviceID;
            }

            /**
             * Return an ID (String) that identifies this extractor
             *
             * @return an ID that identifies the general functionality of this extractor
             */
            virtual const std::string& getExtractorID() { return extractorID; }

            /**
             * Return an ID (String) that identifies this extractor
             *
             * @return an ID that identifies the specific mode in which the extractor is running
             */
            virtual const std::string& getExtractorModeID() { return extractorModeID; }

            /**
             * Returns the version of the extractor
             *
             * @return an ID that identifies the specific mode in which the extractor is running
             */
            virtual const std::string& getExtractorVersion() { return extractorVersion; }

            /**
            * Return the type of output produced by this service as symbolic identifier. In the first version of the API, this
            * is simply an arbitrary string (e.g. MIME type)
            *
            * @return a symbolic identifier representing the output type of this service
            */
            virtual const std::string& getProvides() const { return provides; }


            /**
            * Return the type of input required by this service as symbolic identifier. In the first version of the API, this
            * is simply an arbitrary string (e.g. MIME type)
            *
            * @return  a symbolic identifier representing the input type of this service
            */
            virtual const std::string& getRequires() const { return requires; }


            /**
            * Return the queue name that should be used by the messaging infrastructure for this service. If explicitly set,
            * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
            * balancing.
            *
            * The implementation can return null, in which case the event API will choose a random queue name.
            *
            * @return a string identifying the queue name this service wants to use
            */
            virtual const std::string getQueueName() const final { return queue; }


            /**
            * Call this service for the given content item and object. This method is called by the event manager whenever
            * a new analysis event for this service has been received in its queue. The API takes care of automatically
            * resolving the content item in the persistence service.
            *
            * @param resp   a response object that can be used to send back notifications about new objects to the broker
            * @param ci     the content item to analyse
            * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
            */
            virtual void call(mico::event::AnalysisResponse& response,
                              std::shared_ptr< mico::persistence::model::Item > item,
                              std::vector<std::shared_ptr<mico::persistence::model::Resource>> resources,
                              std::map<std::string,std::string>& params) = 0;
        };

    }
}
#endif
