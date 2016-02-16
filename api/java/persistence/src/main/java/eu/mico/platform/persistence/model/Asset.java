package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;

public interface Asset {

    URI getLocation();

    String getFormat();
}
