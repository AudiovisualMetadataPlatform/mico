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
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.storage.util.VFSUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * An implementation of the persistence service using an FTP file system
 * and a Marmotta triple store for representing  item data.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public class PersistenceServiceImpl implements PersistenceService {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceImpl.class);

    public static MICOIDGenerator idGenerator;

    private java.net.URI marmottaServerURI;
    private java.net.URI partURI;

    /**
     * Persistence service the default credentials
     *
     * @param host mico platform address
     */
    public PersistenceServiceImpl(String host) throws java.net.URISyntaxException {
        this(new java.net.URI("http://" + host + ":8080/marmotta"), new java.net.URI("hdfs://" + host));
    }


    public PersistenceServiceImpl(java.net.URI marmottaServerURI, java.net.URI partURI) {
        System.setProperty("marmottaServerURI", marmottaServerURI.toString());
        this.marmottaServerURI = marmottaServerURI.normalize();
        this.partURI = partURI.normalize();

        idGenerator = new MICOIDGenerator(marmottaServerURI.toString());

        // configurate Anno4j
        try {
            Anno4j.getInstance().setRepository(getMetadata().getRepository());
            Anno4j.getInstance().setIdGenerator(idGenerator);
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        VFSUtils.configure();
    }

    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing items.
     *
     * @return
     */
    @Override
    public Metadata getMetadata() throws RepositoryException {
        return new MarmottaMetadata(marmottaServerURI);
    }

    private java.net.URI getContext() {
        return marmottaServerURI;
    }

    /**
     * Create a new item with a random URI and return it. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created Item
     */
    @Override
    public Item createItem() throws RepositoryException {

        UUID id = UUID.randomUUID();
        URIImpl itemURI = new URIImpl(URITools.normalizeURI(marmottaServerURI.toString() + "/" + id.toString()));

        Item ci = new MarmottaItem(marmottaServerURI, partURI, itemURI);

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createItem", of("g", marmottaServerURI.toString(), "ci", ci.getURI().stringValue())));

            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        } finally {
            m.close();
        }
    }

    /**
     * Create a new item with the given URI and return it. The item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id the URI of the Item
     * @return a handle to the newly created Item
     */
    @Override
    public Item createItem(URI id) throws RepositoryException {
        Item ci = new MarmottaItem(marmottaServerURI, partURI, id);

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createItem", of("g", marmottaServerURI.toString(), "ci", ci.getURI().stringValue())));
            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        } finally {
            m.close();
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
        // check if part exists
        Metadata m = getMetadata();

        try {
            if (m.ask(createNamed("askItem", of("g", marmottaServerURI.toString(), "ci", id.stringValue())))) {
                return new MarmottaItem(marmottaServerURI, partURI, id);
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed", e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed", e);
        } finally {
            m.close();
        }
    }

    /**
     * Delete the item with the given URI. If the item does not exist, do nothing.
     */
    @Override
    public void deleteItem(URI id) throws RepositoryException {

        // TODO
        // For now errors (non existing files) have to be ignored, as a part must have a data part.

        Item item = getItem(id);
        for (Part part : item.listParts()) {
            try {
                item.deletePart(part.getURI());
            } catch (IOException e) {
                log.error("Error deleting part {} from storage: {}", part.getURI().toString(), e);
            }
        }

        Metadata m = getMetadata();

        try {
            // delete from global metadata first
            m.update(createNamed("deleteItem", of("g", marmottaServerURI.toString(), "ci", id.stringValue())));

            // delete the metadata, execution, and results context for the item
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaItem.SUFFIX_METADATA)));
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaItem.SUFFIX_EXECUTION)));
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaItem.SUFFIX_RESULT)));

        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        } finally {
            m.close();
        }

    }

    /**
     * Return an iterator over all currently available items.
     *
     * @return iterable
     */
    @Override
    public Iterable<Item> getItems() throws RepositoryException {
        Metadata m = getMetadata();

        try {
            final TupleQueryResult r = m.query(createNamed("listItems", of("g", marmottaServerURI.toString())));


            return new Iterable<Item>() {
                @Override
                public Iterator<Item> iterator() {
                    return new Iterator<Item>() {
                        @Override
                        public boolean hasNext() {
                            try {
                                return r.hasNext();
                            } catch (QueryEvaluationException e) {
                                return false;
                            }
                        }

                        @Override
                        public Item next() {
                            try {
                                BindingSet s = r.next();

                                URI ci = (URI) s.getValue("p");

                                return new MarmottaItem(marmottaServerURI, partURI, ci);

                            } catch (QueryEvaluationException e) {
                                return null;
                            }
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("removing elements not supported");
                        }
                    };
                }
            };

        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed", e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed", e);
        } finally {
            m.close();
        }

    }


}
