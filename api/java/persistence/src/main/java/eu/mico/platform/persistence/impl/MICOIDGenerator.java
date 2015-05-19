package eu.mico.platform.persistence.impl;

import com.github.anno4j.persistence.IDGenerator;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;

import java.util.UUID;

public class MICOIDGenerator implements IDGenerator {

    String marmottaServerUrl;

    public MICOIDGenerator(String marmottaServerUrl) {
        this.marmottaServerUrl = marmottaServerUrl;
    }

    @Override
    public Resource generateID() {
        return new URIImpl(marmottaServerUrl + "/" + UUID.randomUUID());
    }
}
