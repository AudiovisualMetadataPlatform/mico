package eu.mico.platform.broker.impl;

import eu.mico.platform.broker.api.MICOBroker;

/**
 * MICO Message Broker, orchestrating the communication between analysis services and the analysis workflow for content
 * items using RabbitMQ.
 *
 * Maintains a dependency graph of registered services based on the input and output they provide. For each content item,
 * uses this dependency graph to execute the analysis workflow.
 *
 * Maintains the following RabbitMQ exchanges:
 * - service_registry: an exchange where the event api sends new service registration events
 * - service_discovery: an exchange where the message broker sends a discovery request on startup and event managers
 *   respond with their service lists
 *
 * Maintains the following RabbitMQ queues:
 * - content item input queue: the broker creates the queue if it does not exist and registers itself as a consumer for
 *                             newly injected content items
 * - content item replyto queue: the broker creates a new temporary queue for each content item it processes and sets it as
 *                             replyto queue for services analysing this content item; it registers itself as consumer
 *                             so it can forward results to the next analysers in the analysis graph
 * - registry queue bound to service_registry: the broker registers itself as consumer to get notified about newly
 *   registered services
 * - replyto queue for each service discovery event: temporarily created when a service discovery is in process; event
 *   managers will respond to this queue about their services; the broker registers itself as consumer to get notified;
 *   queue is cleaned up automatically when it is no longer used.
 *
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MICOBrokerImpl implements MICOBroker {

    private String host;
    private String user;
    private int rabbitPort;
    private int marmottaPort;

}
