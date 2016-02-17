/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceBuilder;
import eu.mico.platform.storage.util.VFSUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of the persistence service using an FTP file system
 * and a Marmotta triple store for representing  item data.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public class PersistenceServiceAnno4j implements PersistenceService {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceAnno4j.class);

    private StorageService storage;
    private java.net.URI sparqlServerURI;
    private Anno4j anno4j;

    public PersistenceServiceAnno4j(java.net.URI sparqlServerURI, java.net.URI storageHost) {
        this.sparqlServerURI = sparqlServerURI.normalize();
        this.storage = StorageServiceBuilder.buildStorageService(storageHost.normalize());

        IDGeneratorAnno4j idGenerator = new IDGeneratorAnno4j(sparqlServerURI.toString());
        SPARQLRepository sparqlRepository = new SPARQLRepository(sparqlServerURI.toString() + "/sparql/select", sparqlServerURI.toString() + "/sparql/update");

        try {
            this.anno4j = new Anno4j(sparqlRepository, idGenerator);
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
        }

        VFSUtils.configure();
    }

    /**
     * For testing purpose. Starts an in memory sparql repository.
     */
    public PersistenceServiceAnno4j(java.net.URI storageHost) throws URISyntaxException {
        this.sparqlServerURI = new java.net.URI("http://localhost/mem").normalize();
        this.storage = StorageServiceBuilder.buildStorageService(storageHost.normalize());

        IDGeneratorAnno4j idGenerator = new IDGeneratorAnno4j(sparqlServerURI.toString());

        try {
            this.anno4j = new Anno4j(idGenerator);
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
        }

        VFSUtils.configure();
    }

    /**
     * Create a new item with a random URI and return it. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created Item
     */
    @Override
    public Item createItem() throws RepositoryException {
        try {
            ItemMMM itemMMM = anno4j.createObject(ItemMMM.class);
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            itemMMM.setSerializedAt(dateTime);
            anno4j.persist(itemMMM, new URIImpl(itemMMM.getResourceAsString()));

            return new ItemAnno4j(itemMMM, this);
        } catch (IllegalAccessException e) {
            throw new RepositoryException("Illegal access", e);
        } catch (InstantiationException e) {
            throw new RepositoryException("Could not instantiate", e);
        }
    }

    /**
     * Return the item with the given URI if it exists. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the Item with the given URI, or null if it does not exist
     */
    @Override
    public Item getItem(URI id) throws RepositoryException {
        ItemMMM itemMMM = anno4j.findByID(ItemMMM.class, id.toString());
        return new ItemAnno4j(itemMMM, this);
    }

    /**
     * Delete the item with the given URI. If the item does not exist, do nothing.
     */
    @Override
    public void deleteItem(URI id) throws RepositoryException {
        anno4j.clearContext(id);
    }

    /**
     * Return an iterator over all currently available items.
     *
     * @return iterable
     */
    @Override
    public Iterable<? extends Item> getItems() throws RepositoryException {
        List<ItemAnno4j> itemsAnno4j = new ArrayList<>();
        List<ItemMMM> itemsMMM = anno4j.findAll(ItemMMM.class);

        for (ItemMMM itemMMM : itemsMMM) {
            itemsAnno4j.add(new ItemAnno4j(itemMMM, this));
        }
        return itemsAnno4j;
    }

    @Override
    public StorageService getStorage() {
        return storage;
    }

    @Override
    public Anno4j getAnno4j() {
        return anno4j;
    }
}