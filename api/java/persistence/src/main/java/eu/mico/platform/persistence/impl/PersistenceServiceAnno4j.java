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
import com.github.anno4j.querying.QueryService;

import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceBuilder;

import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
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

    private final StorageService storage;
    private final java.net.URI sparqlBaseURI;
    private final Anno4j anno4j;
    private final String storagePrefix;

    public PersistenceServiceAnno4j(java.net.URI sparqlBaseURI, java.net.URI storageHost) {
        log.info("Sparql service URI : {}", sparqlBaseURI);
        this.sparqlBaseURI = sparqlBaseURI.normalize();

        log.info("Build storage service for : {}", storageHost);
        this.storage = StorageServiceBuilder.buildStorageService(storageHost.normalize());
        this.storagePrefix = storageHost.normalize().toString();

        IDGeneratorAnno4j idGenerator = new IDGeneratorAnno4j(sparqlBaseURI.toString());
        SPARQLRepository sparqlRepository = new SPARQLRepository(sparqlBaseURI.toString() + "/sparql/select", sparqlBaseURI.toString() + "/sparql/update");

        try {
            this.anno4j = new Anno4j(sparqlRepository, idGenerator, null);
        } catch (RepositoryException | RepositoryConfigException e) {
            throw new IllegalStateException("Couldn't instantiate Anno4j");
        }

    }

    /**
     * For testing purpose. Starts an in memory sparql repository.
     */
    public PersistenceServiceAnno4j() throws URISyntaxException {
        log.info("Create local and in-memory configuration for Sparql service and storage service");
        this.sparqlBaseURI = new java.net.URI("http://localhost/mem").normalize();
        this.storage = StorageServiceBuilder.buildStorageService(ClassLoader.getSystemResource("").toURI());
        this.storagePrefix = ClassLoader.getSystemResource("").toURI().normalize().toString();

        IDGeneratorAnno4j idGenerator = new IDGeneratorAnno4j(sparqlBaseURI.toString());

        try {
            this.anno4j = new Anno4j(idGenerator);
        } catch (RepositoryException | RepositoryConfigException e) {
            throw new IllegalStateException("Couldn't instantiate Anno4j");
        }
    }

    /**
     * Create a new item with a random URI and return it. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created Item
     */
    @Override
    public Item createItem() throws RepositoryException {
        ObjectConnection itemConn = null;
        boolean error = true;
        try {
            itemConn = anno4j.getObjectRepository().getConnection();
            itemConn.begin();
            //we need to do a 2 step creation to ensure that the rdf:type goes into the correct context
            RDFObject itemObj = itemConn.getObjectFactory().createObject(IDGenerator.BLANK_RESOURCE, ItemMMM.class);
            URI itemURI = (URI)itemObj.getResource(); //if this is not a URI use a different IDGenerator!
            setContext(itemConn, itemURI); //now set the correct context on the connection
            ItemMMM itemMMM = itemConn.addDesignation(itemObj, ItemMMM.class); //add the correct rdf:type
            //and set all the other required metadata
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            itemMMM.setSerializedAt(dateTime);
            itemConn.addObject(itemMMM); //store the new item
            log.trace("Created Item <{}>", itemMMM.getResourceAsString());
            error = false;
            return new ItemAnno4j(itemMMM, this);
        } catch (RepositoryException | RuntimeException e){
            error = true;
            throw e;
        } finally {
            if(itemConn != null){
                if(error){
                    itemConn.rollback(); //rollback any triples created during this method
                    itemConn.close(); //in case we have not succeeded we can close the connection
                } else {
                    itemConn.commit(); //commit the item before returning
                }
            } //failed to open connection
        }
    }


    /**
     * Return the item with the given URI if it exists. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id Id of the item to retrieve
     * @return a handle to the Item with the given URI, or null if it does not exist
     */
    @Override
    public Item getItem(URI id) throws RepositoryException {
        //we need to create a ObjectConnection and configure it for the created item
        ObjectConnection itemConn = anno4j.getObjectRepository().getConnection();
        setContext(itemConn, id);
        //we do not parse here a explicit class as a getter method assumes that
        //the parsed resource already has the necessary rdf:type assigned!
        Object object = itemConn.getObject(id);
        //if not we will get a ClssCastException in the next line
        return new ItemAnno4j(ItemMMM.class.cast(object), this);
    }

    /**
     * Delete the item with the given URI. If the item does not exist, do nothing.
     */
    @Override
    public void deleteItem(URI id) throws RepositoryException {
        anno4j.clearContext(id);
        log.trace("Deleted item with id {} including all triples in the corresponding context graph", id.toString());
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
    public String getStoragePrefix() {
        return storagePrefix;
    }
    
    /**
     * Sets the read/insert and remove context on the parsed conntection to the parsed URI
     * @param con
     * @param context
     */
    private void setContext(ObjectConnection con, URI context) {
        con.setReadContexts(context);
        con.setInsertContext(context);
        con.setRemoveContexts(context);
    }

    @Override
    public QueryService createQuery(URI context) throws RepositoryException {
        return anno4j.createQueryService(context);
    }
}
