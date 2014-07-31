#ifndef HAVE_ANALYSIS_SERVICE_H
#define HAVE_ANALYSIS_SERVICE_H 1

#include <string>

#include "rdf_model.hpp"
#include "ContentItem.hpp"

namespace mico {
namespace event {

using std::string;
using namespace mico::rdf::model;
using namespace mico::persistence;


/**
 * Interface to be implemented by services. Consists of some informational methods as well as a callback which is called
 * whenever a new event for this service has been received.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */	
class AnalysisService {

public:
    /**
     * Return a unique ID (URI) that identifies this service and its functionality.
     *
     * @return a unique ID identifying this service globally
     */
    virtual const URI& getServiceID() const = 0;


    /**
     * Return the type of output produced by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return a symbolic identifier representing the output type of this service
     */
    virtual const string& getProvides() const = 0;


    /**
     * Return the type of input required by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return  a symbolic identifier representing the input type of this service
     */
    virtual const string& getRequires() const = 0;


    /**
     * Return the queue name that should be used by the messaging infrastructure for this service. If explicitly set,
     * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
     * balancing.
     *
     * The implementation can return null, in which case the event API will choose a random queue name.
     *
     * @return a string identifying the queue name this service wants to use
     */
    virtual const string& getQueueName() const = 0;


    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp   a response object that can be used to send back notifications about new objects to the broker
     * @param ci     the content item to analyse
     * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
    virtual void call(std::function<void(const ContentItem& ci, const URI& object)> resp, ContentItem& ci, URI& object) = 0;
	
};	
	
}
}
#endif