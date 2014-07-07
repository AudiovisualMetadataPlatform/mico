package eu.mico.marmotta.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.openrdf.model.URI;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Contextual Base Web Service
 *
 * @author sebastian.schaffert.redlink.co
 * @author sergio.fernandez@redlink.co
 */
public abstract class ContextualWebServiceBase {

    public final static String IN = "in";

    public final static String OUT = "out";

    @Inject
    private Logger log;

    @Inject
    protected ConfigurationService configurationService;

    @Inject
    protected ContextService contextService;

    protected Map<String, String> outputMapper;

    public ContextualWebServiceBase() {
        outputMapper = new HashMap<String, String>();
        outputMapper.put("json", "application/ld+json");
        outputMapper.put("rdf", "application/rdf+xml");
        outputMapper.put("ttl", "text/turtle");
        outputMapper.put("n3", "text/n3");
        outputMapper.put("html", "text/html");
    }

    public ContextualWebServiceBase(Map<String, String> outputMapper) {
        this.outputMapper = new HashMap<String, String>(outputMapper);
    }

    /**
     * Resolve the URI of the contexts with the given IDs.
     *
     * @param contexts
     * @return
     */
    protected URI[] resolveContexts(String contexts) {
        List<URI> ids = new ArrayList<URI>();
        try {
            if (contexts.contains(",")) {
                //resolving 1..n contexts
                for (String contextId : contexts.split(",")) {
                    ids.add(resolveContext(contextId));
                }
            } else {
                //actually only one to resolved
                ids.add(resolveContext(contexts));
            }
        } catch (MarmottaException e) {
            log.error("Error resolving context id '{}': {}", e.getMessage());
            throw new RuntimeException("Error resolving context id '{}': {}" + e.getMessage(), e);
        }
        if (ids.size() == 0) {
            throw new RuntimeException("invalid context ids");
        }
        return ids.toArray(new URI[ids.size()]);
    }

    /**
     * Resolve the URI of the context with the given ID.
     *
     * @param contextId
     * @return
     */
    protected URI resolveContext(String contextId) throws MarmottaException {
        if (contextId.contains(",")) {
            //forcing to resolve just the first one
            contextId = contextId.split(",")[0];
        }
        try {
            final URI context = contextService.createContext(configurationService.getBaseUri() + contextId);
            if (context == null) {
                throw new MarmottaException("context not resolved");
            }
            return context;
        } catch (URISyntaxException e) {
            throw new MarmottaException(e.getMessage());
        }
    }

    protected String checkInOutParameter(String out) {
        if (StringUtils.isNotBlank(out) && outputMapper.containsKey(out)) {
            return outputMapper.get(out);
        } else {
            return out;
        }
    }

    protected ContentType performContentNegotiation(String format, String accept, Collection<String> producedTypes) {
        List<ContentType> producedContentTypes = MarmottaHttpUtils.parseStringList(producedTypes);
        return performContentNegotiation(format, accept, producedContentTypes);

    }

    protected ContentType performContentNegotiation(String format, String accept, List<ContentType> producedContentTypes) {
        List<ContentType> acceptedContentTypes;
        if (StringUtils.isNotBlank(format)) {
            //forced format
            if (outputMapper.containsKey(format)) {
                //short name
                acceptedContentTypes = MarmottaHttpUtils.parseAcceptHeader(outputMapper.get(format));
            } else {
                //then should be a mimetype
                acceptedContentTypes = MarmottaHttpUtils.parseAcceptHeader(format);
            }
        } else {
            //from the accept header
            acceptedContentTypes = MarmottaHttpUtils.parseAcceptHeader(StringUtils.defaultString(accept, ""));
        }

        //actual content negotiation
        return MarmottaHttpUtils.bestContentType(producedContentTypes, acceptedContentTypes);
    }

}
