package eu.mico.platform.camel;

import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import de.fraunhofer.idmt.camel.MicoCamel;
import eu.mico.platform.event.model.Event.AnalysisRequest;

public abstract class TestBase extends CamelTestSupport {

    protected static final String SAMPLE_HTML = "sample.html";
    protected static final String SAMPLE_PNG = "sample.png";
    protected static final String SAMPLE_MP4 = "sample-video.mp4";
    protected static final String TEST_DATA_FOLDER = "src/test/resources/data/";
    protected static SimpleDateFormat isodate = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'", DateFormatSymbols.getInstance(Locale.US));
    protected static MicoCamel micoCamel;

    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public TestBase() {
        super();
    }

    public static URI getServiceID() {
        return new URIImpl("http://example.org/services/CAMEL-TEST-injector");
    }

    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    protected Exchange createExchange(String itemUri) {
        return createExchange(itemUri, itemUri, getServiceID().stringValue());
    }


    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    protected Exchange createExchange(String itemUri, String partUri) {
        return createExchange(itemUri, partUri, getServiceID().stringValue());
    }

    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange(String itemUri, String partUri, String service) {
        Exchange exchange = context.getEndpoint("direct:a").createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        AnalysisRequest event = AnalysisRequest.newBuilder()
                .setItemUri(itemUri)
                .addPartUri(partUri)
                .setServiceId(service)
                .build();
        msg.setBody(event.toByteArray());
        return exchange;
    }

}