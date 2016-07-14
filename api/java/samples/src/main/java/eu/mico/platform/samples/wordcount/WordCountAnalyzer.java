/**
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
package eu.mico.platform.samples.wordcount;

import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.targets.SpecificResource;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.AnalysisServiceUtil;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.io.IOUtils;


import org.openrdf.annotations.Iri;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a sample analyzer demonstrating how to use the MICO Java Client API to implement analyzers. It takes
 * a plain text as input and generates a word count as output, using an RDF property to attach to a new content part.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class WordCountAnalyzer implements AnalysisService {
    
    private static Boolean simulateSlow = true;

    private static Logger log = LoggerFactory.getLogger(WordCountAnalyzer.class);

    private static SimpleDateFormat isodate = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'", DateFormatSymbols.getInstance(Locale.US));
    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

	@Override
	public String getExtractorID() {
		return "wordcount";
	}


	@Override
	public String getExtractorModeID() {
		return  "regex";
	}


	@Override
	public String getExtractorVersion() {
		return "2.0.0";
	}
 
    @Override
    public String getProvides() {
        return "mico/wordcount";
    }

    @Override
    public String getRequires() {
        return "text/plain";
    }

    @Override
    public void call(AnalysisResponse analysisResponse, Item item, List<Resource> resourceList, Map<String, String> params) throws RepositoryException, AnalysisException, IOException {
        ObjectConnection con = item.getObjectConnection();
        ObjectFactory factory = con.getObjectFactory();
        if(resourceList == null || resourceList.size() != 1) {
            throw new IllegalArgumentException("A call should contain one item.");
        }

        Resource resource = resourceList.get(0);
        if( resource == null || !resource.hasAsset()) {
            throw new IllegalArgumentException("Resource to analyze MUST HAVE an asset.");
        }

        log.info("Retrieved analysis call for {}", resource.getURI());

        Asset asset = item.getAsset();
        if(!getRequires().equals(asset.getFormat())){
            log.warn("The asset format should be: {} but is {}", getRequires(), asset.getFormat());
        }

        // get the input stream and read it into a string
        String text = IOUtils.toString(asset.getInputStream(), "utf-8");
        log.debug("Loaded text of {} to count words", resource.getURI());

        // count the words using a regular expression pattern
        Pattern p_wordcount = Pattern.compile("\\w+");
        Matcher m = p_wordcount.matcher(text);

        // we are progressing ... inform broker
        analysisResponse.sendProgress(item, resource.getURI(), 0.25f);
        if (simulateSlow == true) {
            simulateLongTask(analysisResponse, item, resource.getURI());
        }

        int count;
        for(count = 0; m.find(); count++);

        log.debug("Counted {} words in {}", count, resource.getURI());

        // create a new part for assigning the metadata
        Part part = item.createPart(AnalysisServiceUtil.getServiceID(this));
        part.setSyntacticalType(getProvides());

        // create example wordcount body and setting the result of the analyzer
        WordCountBody wordCountBody = con.addDesignation(
                factory.createObject(IDGenerator.BLANK_RESOURCE, WordCountBody.class),
                WordCountBody.class);
        wordCountBody.setCount(count);
        part.setBody(wordCountBody);

        // create the target and set a reference to the part/item on which the body refers to
        SpecificResource specificResource = con.addDesignation(
                factory.createObject(IDGenerator.BLANK_RESOURCE, SpecificResource.class),
                SpecificResource.class);
        specificResource.setSource(resource.getRDFObject());

        // adding the target to the part
        part.addTarget(specificResource);

        // adding the input
        part.addInput(resource);
        analysisResponse.sendNew(item, part.getURI());

        analysisResponse.sendFinish(item);
        log.debug("Done for {}", resource.getURI());
    }

    /**
     * This function uses Thread.sleep() and sendProgress() to simulate long running analyze
     * process
     * 
     * @param analysisResponse
     * @param item
     * @param uri
     */
    private void simulateLongTask(AnalysisResponse analysisResponse, Item item, URI uri) {
        try {
            log.debug("debug is enabled, sleep 5 seconds and send next progress info");
            Thread.sleep(5000);
            analysisResponse.sendProgress(item, uri, 0.50f);
            Thread.sleep(5000);
            analysisResponse.sendProgress(item, uri, 0.75f);
            analysisResponse.sendProgress(item, uri, 0.76f);
            analysisResponse.sendProgress(item, uri, 0.77f);
            analysisResponse.sendProgress(item, uri, 0.78f);
            log.debug("... progress updated, sleep 5 seconds again");
            Thread.sleep(5000);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }


    // usage: java WordCountAnalyzer <hostname> <user> <password>
    public static void main(String[] args) {
        if(args.length != 3) {
            System.err.println("Usage: java WordCountAnalyzer <hostname> <user> <password>");
            System.exit(1);
        }

        String mico_host = args[0];
        String mico_user = args[1];
        String mico_pass = args[2];

        try {
            // create event manager instance, providing the correct host, user and password, and initialise it
            EventManager eventManager = new EventManagerImpl(mico_host, mico_user, mico_pass);
            eventManager.init();

            // create analyzer service instance and register it with event manager
            WordCountAnalyzer svc_wc = new WordCountAnalyzer();
            eventManager.registerService(svc_wc);


            // keep running service in the background, and wait for user command "q" on the frontent to terminate
            // service (other approaches might be more sensible for a service, e.g. commons-daemon)
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            char c = ' ';
            while(Character.toLowerCase(c) != 'q') {
                System.out.print("enter 'q' to quit: ");
                System.out.flush();

                c = in.readLine().charAt(0);
            }

            // unregister service before quiting
            eventManager.unregisterService(svc_wc);

            // shutdown event manager properly
            eventManager.shutdown();

            System.out.println("WordCountAnalyzer shutdown completed");
            // DONE
        } catch (IOException e) {
            log.error("error while accessing event manager:",e);
        } catch (URISyntaxException e) {
            log.error("invalid hostname:", e);
        } catch (TimeoutException e) {
            log.error("fetching configuration timed out:", e);
        }

    }

    @Iri("http://www.mico-project.eu/wordcount-body")
    public interface WordCountBody extends Body {
        @Iri("http://www.mico-project.eu/wordcount-body#count")
        void setCount(int count);

        @Iri("http://www.mico-project.eu/wordcount-body#count")
        int getCount();
    }

    @Iri("http://www.mico-project.eu/wordcount-target")
    public interface WordCountTarget extends Target {

    }
}
