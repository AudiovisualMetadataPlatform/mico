package eu.mico.platform.camel.log;

import java.util.EventObject;

import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;

/**
 * to use this add the event notifier to management strategy of camel context

 * @author sld
 *
 */
public class LoggingSentEventNotifier extends EventNotifierSupport {
 
    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeSentEvent) {
            ExchangeSentEvent sent = (ExchangeSentEvent) event;
            log.info("Took " + sent.getTimeTaken() + " millis to send "+sent.getExchange().getExchangeId()+" to: " + sent.getEndpoint());
        }

        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent compE = (ExchangeCompletedEvent) event;
            log.info("Processing of exchange {} from {} finished. ",compE.getExchange().getExchangeId(),compE.getSource());
        }
 
    }
 
    public boolean isEnabled(EventObject event) {
        // we only want the sent and completed events
        return event instanceof ExchangeSentEvent || event instanceof ExchangeCompletedEvent;
    }
 
    protected void doStart() throws Exception {
        // noop
    }
 
    protected void doStop() throws Exception {
        // noop
    }
 
}
