package de.fraunhofer.idmt.mico;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DummyExtractor implements AnalysisService {

	private static Logger log = LoggerFactory.getLogger(DummyExtractor.class);
    private boolean called = false;
    private String source, target;

    private static SimpleDateFormat isodate = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'", DateFormatSymbols.getInstance(Locale.US));
    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public boolean isCalled() {
		return called;
	}

	public void setCalled(boolean called) {
		this.called = called;
	}

	public DummyExtractor(String source, String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public URI getServiceID() {
        return new URIImpl("http://example.org/services/" + StringUtils.capitalize(source) +"-"+ StringUtils.capitalize(target) + "-Service");
    }

    @Override
    public String getProvides() {
        return target;
    }

    @Override
    public String getRequires() {
        return source;
    }

    @Override
    public String getQueueName() {
        return source + "-" + target + "-queue";
    }

    @Override
    public void call(AnalysisResponse resp, ContentItem ci, URI object)
            throws AnalysisException, IOException {
        if (ci == null) {
            log.warn("contentItem is null");
            return;
        }
        if (object == null) {
            log.warn("object is null");
            return;
        }
        log.info("mock analysis request for content item {}, object {}", ci.getURI(), object);
        Content cp = null;
        try {
            cp = ci.createContentPart();
            cp.setType(getProvides());
            cp.setRelation(DCTERMS.CREATOR, getServiceID());
            cp.setRelation(DCTERMS.SOURCE, object);
            cp.setProperty(DCTERMS.CREATED, isodate.format(new Date()));

            OutputStream os = cp.getOutputStream();
            try{
                ci.getContentPart(object).getInputStream();
                IOUtils.copy(ci.getContentPart(object).getInputStream(), os);
            }catch(Exception e){
                log.warn("unable to access content part data",e);
            }
            os.write(("\nData added by: " + getServiceID() + "\n").getBytes());
            os.flush();
            os.close();
            log.info("new contentpart added: {}", cp.getURI());

            resp.sendMessage(ci, cp.getURI());
            setCalled(true);
        } catch (RepositoryException e) {
            throw new AnalysisException("could not access triple store");
        }

    }


}
