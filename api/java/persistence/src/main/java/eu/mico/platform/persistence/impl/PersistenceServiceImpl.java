/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.persistence.util.IDUtils;
import eu.mico.platform.persistence.util.VFSUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.UUID;

import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * An implementation of the persistence service using an FTP file system
 * and a Marmotta triple store for representing content item data.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class PersistenceServiceImpl implements PersistenceService {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceImpl.class);

    public static MICOIDGenerator idGenerator;

    private String marmottaServerUrl;
    private String contentUrl;

    /**
     * Persistence service over localhost with the default credentials
     */
    public PersistenceServiceImpl() {
        this("127.0.0.1");
    }

    /**
     * Persistence service the default credentials
     *
     * @param host mico platform address
     */
    public PersistenceServiceImpl(String host) {
        this(host, "mico", "pass123");
    }

    /**
     * Persistence service
     *
     * @param host mico platform address
     * @param user
     * @param password
     */
    public PersistenceServiceImpl(String host, String user, String password) {
        this("http://" + host + ":8080/marmotta", "ftp://" + user + ":" + password + "@" + host);
    }

    public PersistenceServiceImpl(String marmottaServerUrl, String contentUrl) {

        this.marmottaServerUrl = marmottaServerUrl;
        this.contentUrl        = contentUrl;

        idGenerator = new MICOIDGenerator(marmottaServerUrl);

        // configurate Anno4j
        try {
            Anno4j.getInstance().setRepository(getMetadata().getRepository());
            Anno4j.getInstance().setIdGenerator(PersistenceServiceImpl.idGenerator);
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        VFSUtils.configure();
    }

    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing content items.
     *
     * @return
     */
    @Override
    public Metadata getMetadata() throws RepositoryException {
        return new MarmottaMetadata(marmottaServerUrl);
    }

    private String getContext() {
        return marmottaServerUrl;
    }

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem() throws RepositoryException {

        UUID uuid = IDUtils.generatedRandomUuid();

        ContentItem ci = new MarmottaContentItem(marmottaServerUrl,contentUrl,uuid);

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createContentItem", of("g", marmottaServerUrl, "ci", ci.getURI().stringValue())));

            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem(URI id) throws RepositoryException {
        ContentItem ci = new MarmottaContentItem(marmottaServerUrl,contentUrl,id);


        Metadata m = getMetadata();
        try {
            m.update(createNamed("createContentItem", of("g", marmottaServerUrl, "ci", ci.getURI().stringValue())));

            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    @Override
    public ContentItem getContentItem(URI id) throws RepositoryException {
        // check if part exists
        Metadata m = getMetadata();

        try {
            if(m.ask(createNamed("askContentItem", of("g", marmottaServerUrl, "ci", id.stringValue())))) {
                return new MarmottaContentItem(marmottaServerUrl, contentUrl,id);
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:",e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:",e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        }
    }

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    @Override
    public void deleteContentItem(URI id) throws RepositoryException {

        // delete the content parts binary data
        // TODO

        Metadata m = getMetadata();

        try {
            // delete from global metadata first
            m.update(createNamed("deleteContentItem", of("g", marmottaServerUrl, "ci", id.stringValue())));

            // delete the metadata, execution, and results context for the content item
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaContentItem.SUFFIX_METADATA)));
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaContentItem.SUFFIX_EXECUTION)));
            m.update(createNamed("deleteGraph", of("g", id.stringValue() + MarmottaContentItem.SUFFIX_RESULT)));

        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }

    }

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    @Override
    public Iterable<ContentItem> getContentItems() throws RepositoryException {
        Metadata m = getMetadata();

        try {
            final TupleQueryResult r = m.query(createNamed("listContentItems", of("g", marmottaServerUrl)));


            return new Iterable<ContentItem>() {
                @Override
                public Iterator<ContentItem> iterator() {
                    return new Iterator<ContentItem>() {
                        @Override
                        public boolean hasNext() {
                            try {
                                return r.hasNext();
                            } catch (QueryEvaluationException e) {
                                return false;
                            }
                        }

                        @Override
                        public ContentItem next() {
                            try {
                                BindingSet s = r.next();

                                URI ci = (URI) s.getValue("p");

                                return new MarmottaContentItem(marmottaServerUrl,contentUrl, ci);

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
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        }

    }


}
