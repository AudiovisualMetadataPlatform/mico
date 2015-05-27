package eu.mico.platform.storage.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Content Item
 *
 * @author Sergio fernández
 */
public class ContentItem {

    private String id;
    private Set<Content> parts;

    public ContentItem(String id) {
        this.id = id;
        parts = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Content> getParts() {
        return Collections.unmodifiableSet(parts);
    }

    public void setParts(Set<Content> parts) {
        this.parts = parts;
    }

    public boolean addPart(Content part) {
        return parts.add(part);
    }

}