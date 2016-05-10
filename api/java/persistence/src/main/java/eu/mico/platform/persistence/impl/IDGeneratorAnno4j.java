package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.util.URITools;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.Set;
import java.util.UUID;

public class IDGeneratorAnno4j implements IDGenerator {

    private String sparqlBaseURI;

    public IDGeneratorAnno4j(String sparqlBaseURI) {
        this.sparqlBaseURI = sparqlBaseURI;
    }

    @Override
    public Resource generateID(Set<URI> types) {
        return new URIImpl(URITools.normalizeURI(sparqlBaseURI + "/" + UUID.randomUUID()));
    }
}
