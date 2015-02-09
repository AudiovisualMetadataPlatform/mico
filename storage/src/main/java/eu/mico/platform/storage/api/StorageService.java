package eu.mico.platform.storage.api;

import eu.mico.platform.storage.model.ContentItem;

import java.util.Collection;

/**
 * Storage Service
 *
 * @author Sergio Fernández
 */
public interface StorageService {

    Collection<ContentItem> list();

}
