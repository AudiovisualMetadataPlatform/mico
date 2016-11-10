/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.camel;

import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_STARTING_DIRECT;
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

    protected static final long CONTEXT_DELAYER = 0L;
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
    protected Exchange createExchange(String itemUri, String directUri) {
        return createExchange(itemUri, itemUri, directUri, getServiceID().stringValue());
    }


    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    protected Exchange createExchange(String itemUri, String partUri, String directUri){
        return createExchange(itemUri, partUri, directUri, getServiceID().stringValue());
    }

    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange(String itemUri, String partUri, String directUri, String service) {
        Exchange exchange = context.getEndpoint(directUri).createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        msg.setHeader(KEY_STARTING_DIRECT, directUri);
        AnalysisRequest event = AnalysisRequest.newBuilder()
                .setItemUri(itemUri)
                .addPartUri(partUri)
                .setServiceId(service)
                .build();
        msg.setBody(event.toByteArray());
        return exchange;
    }

}