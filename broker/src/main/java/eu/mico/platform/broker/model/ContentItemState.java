package eu.mico.platform.broker.model;

import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent the current processing state of a content item
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContentItemState {

    private ContentItem contentItem;

    private Map<URI, TypeDescriptor> state;
    private ServiceGraph             graph;

    public ContentItemState(ServiceGraph graph, ContentItem contentItem) {
        this.graph       = graph;
        this.contentItem = contentItem;

        this.state = new HashMap<>();
    }


    /**
     * Create the initial state for this content item. For each content part, we look up the vertex in the
     * dependency graph that matches the type, and add an appropriate entry into the state table.
     */
    private void initState() {

    }
}
