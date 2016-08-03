package de.fraunhofer.idmt.mico;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

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
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


/**
 * this test extractor checks if input values like item, object and parameters
 * are set, throws exception otherwise.
 * 
 * @author sld
 *
 */
public class DummyExtractorComplexTest implements AnalysisService {

    public static final String PARAM_OUTPUTS = "outputType";
    private int PART_REPLICAS;
    private static Logger log = LoggerFactory.getLogger(DummyExtractorComplexTest.class);
    private boolean called = false;
    private String source, target;
    private String extractorId, version, mode;

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

    public DummyExtractorComplexTest(String source, String target, int PART_REPLICAS) {
        this(source, target, "mico-extractor-complex-test", "1.0.0", source+"-"+target+"-queue");
        this.PART_REPLICAS = PART_REPLICAS;
    }
	private DummyExtractorComplexTest(String source, String target, String extractorID, String version, String mode) {
        this.source = source;
        this.target = target;
        this.extractorId = extractorID;
        this.version = version;
        this.mode = mode;
    }

    public URI getServiceID() {
        return new URIImpl("http://example.org/services/" + getQueueName());
    }

    @Override
    public String getProvides() {
        return target;
    }

    @Override
    public String getRequires() {
        return source;
    }

    public String getQueueName() {
        return extractorId + "-" + version + "-" + mode;
    }

    @Override
    public String getExtractorID() {
        return extractorId;
    }

    @Override
    public String getExtractorModeID() {
        return mode;
    }

    @Override
    public String getExtractorVersion() {
        return version;
    }

    @Override
    public void call(AnalysisResponse resp, Item item, java.util.List<Resource> objs, Map<String,String> params) throws AnalysisException ,IOException {
        if (item == null) {
            log.warn("Item is null");
            throw new AnalysisException("Item is null");
        }
        if (objs == null || objs.size() < 1) {
            log.warn("Object is null");
            throw new AnalysisException("Object is null");
        }
        if (mode.startsWith("A-") && (params == null || params.size() < 1)) {
            log.warn("Extractor parameters are missing");
            throw new AnalysisException("Extractor parameters are missing");
        }

        Resource obj = objs.get(0);
        if (!obj.getSyntacticalType().contentEquals(source)) {
            throw new AnalysisException("syntactical type ("
                    + obj.getSyntacticalType() + ") of object "
                    + obj.getURI().stringValue() + " is not: " + source);
        }

        log.info("mock analysis request for content item {}, object {}", item.getURI(), obj.getURI());
        Part part = null;
        try {
            String outputTypes = params.get(PARAM_OUTPUTS);
            if(outputTypes == null){
                outputTypes = target;
            }
            
            for (String out : outputTypes.split(";")) {
                ObjectConnection con = item.getObjectConnection();
                ObjectFactory factory = con.getObjectFactory();
                
                DummyExtractorCheckInputBody body;
                if (out.contains("B1")){
                    body = getBody(con, factory, DummyExtractorCheckInputBodyB1.class);
                } else if(out.contains("B2")){
                    body = getBody(con, factory, DummyExtractorCheckInputBodyB2.class);
                } else if(out.contains("C1")){
                    body = getBody(con, factory, DummyExtractorCheckInputBodyC1.class);
                } else if(out.contains("C2")){
                    body = getBody(con, factory, DummyExtractorCheckInputBodyC2.class);
                }else{
                    throw new AnalysisException("Unknown output type");
                }

                for (int i =0 ; i<PART_REPLICAS; i++){
                    part = item.createPart(getServiceID());
                    part.setSyntacticalType(out);
                    part.setSemanticType(getQueueName());
                    part.setInputs(new HashSet<Resource>(objs));
    
                    part.setBody(body);
                    DummyExtractorCheckInputTarget target = getTarget(con, factory);
                    part.addTarget(target);
    
                    log.info("new contentpart added: {}", part.getURI());
                    resp.sendNew(item, part.getURI());
                }
            }
            setCalled(true);
            resp.sendFinish(item);
        } catch (RepositoryException e) {
            throw new AnalysisException("could not access triple store");
        }

    }

    private <T extends DummyExtractorCheckInputBody> T getBody(ObjectConnection con,
            ObjectFactory factory, Class<T> type) throws RepositoryException {
        T body = con.addDesignation(factory
                .createObject(IDGenerator.BLANK_RESOURCE,
                        type),
                        type);
        body.setValue(getServiceID().stringValue());
        return body;
    }

    private DummyExtractorCheckInputTarget getTarget(ObjectConnection con,
            ObjectFactory factory) throws RepositoryException {
        DummyExtractorCheckInputTarget target = con.addDesignation(factory
                .createObject(IDGenerator.BLANK_RESOURCE,
                        DummyExtractorCheckInputTarget.class),
                DummyExtractorCheckInputTarget.class);
        return target;
    }

    public interface DummyExtractorCheckInputBody extends Body {
    @Iri("http://example.org/services/dummy-extractor-check-input-body#value")
    void setValue(String value);

    @Iri("http://example.org/services/dummy-extractor-check-input-body#value")
    String getValue();
    }
    
    @Iri("http://example.org/services/dummy-extractor-check-input-bodyB1")
    public interface DummyExtractorCheckInputBodyB1 extends DummyExtractorCheckInputBody {
        @Iri("http://example.org/services/dummy-extractor-check-input-bodyB1#value")
        void setValue(String value);

        @Iri("http://example.org/services/dummy-extractor-check-input-bodyB1#value")
        String getValue();
    }

    @Iri("http://example.org/services/dummy-extractor-check-input-bodyB2")
    public interface DummyExtractorCheckInputBodyB2 extends DummyExtractorCheckInputBody {
        @Iri("http://example.org/services/dummy-extractor-check-input-bodyB2#value")
        void setValue(String value);

        @Iri("http://example.org/services/dummy-extractor-check-input-bodyB2#value")
        String getValue();
    }

    @Iri("http://example.org/services/dummy-extractor-check-input-bodyC1")
    public interface DummyExtractorCheckInputBodyC1 extends DummyExtractorCheckInputBody {
        @Iri("http://example.org/services/dummy-extractor-check-input-bodyC1#value")
        void setValue(String value);

        @Iri("http://example.org/services/dummy-extractor-check-input-bodyC1#value")
        String getValue();
    }

    @Iri("http://example.org/services/dummy-extractor-check-input-bodyC2")
    public interface DummyExtractorCheckInputBodyC2 extends DummyExtractorCheckInputBody {
        @Iri("http://example.org/services/dummy-extractor-check-input-bodyC2#value")
        void setValue(String value);

        @Iri("http://example.org/services/dummy-extractor-check-input-bodyC2#value")
        String getValue();
    }

    @Iri("http://example.org/services/dummy-extractor-check-input-target")
    public interface DummyExtractorCheckInputTarget extends Target {

    }

}
