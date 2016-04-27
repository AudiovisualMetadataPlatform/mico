package de.fraunhofer.idmt.mico;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.annotations.Iri;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
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
import java.util.Map;
import java.util.TimeZone;


public class DummyExtractor implements AnalysisService {

	private static Logger log = LoggerFactory.getLogger(DummyExtractor.class);
    private boolean called = false;
    private String source, target;
    private String extractorId, version, mode;
    private Map<String,String> parameters;

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
        this(source, target, source+"-"+target+"-queue", "0.0.0", "");
    }
	public DummyExtractor(String source, String target, String extractorID, String version, String mode) {
        this.source = source;
        this.target = target;
        this.extractorId = extractorID;
        this.version = version;
        this.mode = mode;
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
        return extractorId +"-" + version +"-"+ mode;
    }

    @Override
    public void call(AnalysisResponse resp, Item item, java.util.List<Resource> objs, Map<String,String> params) throws AnalysisException ,IOException {
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
        parameters = params;
        Resource obj = objs.get(0);
        try {
            if (!obj.hasAsset()) {
                log.warn("object {} of item {} has not asset", item.getURI(), obj.getURI());
                return;
            }
        } catch (RepositoryException e1) {
            log.warn("unable to acces asset info of {}", obj.getURI());
            return;
        }
        log.info("mock analysis request for content item {}, object {}", item.getURI(), obj.getURI());
        Part part = null;
        try {
            ObjectConnection con = item.getObjectConnection();
            ObjectFactory factory = con.getObjectFactory();
            part = item.createPart(getServiceID());
            part.setSyntacticalType(getProvides());
            part.setSemanticType(getQueueName());
            part.setInputs(new HashSet<Resource>(objs));

            DummyExtractorBody body = con.addDesignation(factory.createObject(
                    IDGenerator.BLANK_RESOURCE, DummyExtractorBody.class),
                    DummyExtractorBody.class);
            body.setValue(getServiceID().stringValue());
            part.setBody(body);
            DummyExtractorTarget target = con.addDesignation(factory
                    .createObject(IDGenerator.BLANK_RESOURCE,
                            DummyExtractorTarget.class),
                    DummyExtractorTarget.class);
            part.addTarget(target);


            Asset asset = part.getAsset();
            log.info("create binary asset for part: [{}]", part.getURI());
            OutputStream os = asset.getOutputStream();
            asset.setFormat(getProvides());
            try{
                if(obj.hasAsset()){
                    InputStream is = obj.getAsset().getInputStream();
                    IOUtils.copy(is, os);
                }
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
        }

    }

    public Map<String,String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String,String> parameters) {
        this.parameters = parameters;
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
