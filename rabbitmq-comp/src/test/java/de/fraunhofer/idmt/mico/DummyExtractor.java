package de.fraunhofer.idmt.mico;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.impl.AnalysisServiceAnno4j;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.annotations.Iri;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;


public class DummyExtractor extends AnalysisServiceAnno4j {

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
    public void call(AnalysisResponse resp, Item item, java.util.List<Resource> objs, java.util.Map<String,String> params) throws AnalysisException ,IOException {
        if (item == null) {
            log.warn("Item is null");
            return;
        }
        if (objs == null) {
            log.warn("object is null");
            return;
        }
        if (objs.size() < 1) {
            log.warn("object is null");
            return;
        }
        Resource obj = objs.get(0);
        try {
            if (!obj.hasAsset()) {
                log.warn("object {} of item {}", item.getURI(), obj.getURI());
                return;
            }
        } catch (RepositoryException e1) {
            log.warn("unable to acces asset info of {}", obj.getURI());
            return;
        }
        log.info("mock analysis request for content item {}, object {}", item.getURI(), obj.getURI());
        Part part = null;
        try {
            part = item.createPart(getServiceID());
            part.setSyntacticalType(getProvides());
            part.setSemanticType(getQueueName());
            part.setInputs(new HashSet<Resource>(objs));

            DummyExtractorBody body = getAnno4j().createObject(DummyExtractorBody.class);
            body.setValue(getServiceID().stringValue());
            part.setBody(body);
            DummyExtractorTarget target = getAnno4j().createObject(DummyExtractorTarget.class);
            part.addTarget(target);


            OutputStream os = part.getAsset().getOutputStream();
            try{
                InputStream is = obj.getAsset().getInputStream();
                IOUtils.copy(is, os);
            }catch(Exception e){
                log.warn("unable to access content part data",e);
            }
            os.write(("\nData added by: " + getServiceID() + "\n").getBytes());
            os.flush();
            os.close();
            log.info("new contentpart added: {}", part.getURI());

            resp.sendFinish(item);
            setCalled(true);
        } catch (RepositoryException e) {
            throw new AnalysisException("could not access triple store");
        } catch (IllegalAccessException | InstantiationException e) {
            throw new AnalysisException("could not create body/target class",e);
        }

    }

    @Iri("http://example.org/services/dummy-exctractor-body")
    public interface DummyExtractorBody extends Body {
        @Iri("http://example.org/services/dummy-exctractor-body#value")
        void setValue(String value);

        @Iri("http://example.org/services/dummy-exctractor-body#value")
        String getValue();
    }

    @Iri("http://example.org/services/dummy-exctractor-target")
    public interface DummyExtractorTarget extends Target {

    }

}
