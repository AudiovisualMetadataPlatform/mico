package eu.mico.platform.camel.aggretation;


import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple Aggregator does nothing
 *
 * @version 
 */
public class SimpleAggregationStrategy implements AggregationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAggregationStrategy.class);
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        // we should never come here
        LOG.warn("It seems we did not finished previous aggregation");
        return newExchange;
    }

}
