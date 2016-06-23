package eu.mico.platform.camel;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.language.Bean;
import org.junit.Test;

import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;

;

/**
 * @author sld
 *
 */
public class AggregationStrategyTest extends TestBase {

    private static SimpleDateFormat isodate = new SimpleDateFormat(
            "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'",
            DateFormatSymbols.getInstance(Locale.US));
    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test(timeout = 10000)
    public void testSimpleAggregationRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(4);

        template.send("direct:a", createExchange("item1", "part1", "direct:a"));
        template.send("direct:a", createExchange("item1", "part2", "direct:a"));
        template.send("direct:a", createExchange("item2", "part1", "direct:a"));
        template.send("direct:a", createExchange("item2", "part2", "direct:a"));
        assertMockEndpointsSatisfied();
    }

    @Test(timeout = 20000)
    public void testItemAggregationRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(3);

        template.send("direct:b", createExchange("item1", "part1", "direct:b"));
        template.send("direct:b", createExchange("item1", "part2", "direct:b"));
        template.send("direct:b", createExchange("item2", "part1", "direct:b"));
        template.send("direct:b", createExchange("item2", "part2", "direct:b"));
        template.send("direct:b", createExchange("item3", "part1", "direct:b"));
        template.send("direct:b", createExchange("item3", "part2", "direct:b"));
        assertMockEndpointsSatisfied();
    }

    @Bean(ref = "simpleAggregatorStrategy")
    public static SimpleAggregationStrategy simpleAggregatorStrategy = new SimpleAggregationStrategy();

    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from("direct:a")
                        .pipeline()
                        .aggregate(header(KEY_MICO_ITEM),
                                simpleAggregatorStrategy).completionSize(1)
                        .to("mock:result");

                from("direct:b")
                        .pipeline()
                        .aggregate(header(KEY_MICO_ITEM),
                                itemAggregatorStrategy).completionSize(2)
                        .to("mock:result");

            }

        };
    }

}
