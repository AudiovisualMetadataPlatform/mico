package eu.mico.platform.camel;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MicoRabbit consumer.
 */
public class MicoRabbitConsumer extends ScheduledPollConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MicoRabbitConsumer.class);
    private final MicoRabbitEndpoint endpoint;

    public MicoRabbitConsumer(MicoRabbitEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();

        // create a message body
        Date now = new Date();
        String body = "Hello World! The time is " + now;
        LOG.info("set message body: {}", body);
		exchange.getIn().setBody(body);

        try {
            // send message to next processor in the route
            getProcessor().process(exchange);
            return 1; // number of messages polled
        } finally {
            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
    }
}
