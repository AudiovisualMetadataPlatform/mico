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

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
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
 * @author Horst Stadler
 */
public class PersistenceServiceImpl implements PersistenceService {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceImpl.class);

    private java.net.URI marmottaServerUrl;
    private java.net.URI contentUrl;


    /**
     * Persistence service the default credentials
     *
     * @param host mico platform address
     */
    public PersistenceServiceImpl(String host)throws java.net.URISyntaxException {
        this(new java.net.URI("http://" + host + ":8080/marmotta"), new java.net.URI("hdfs://" + host));
    }

    /**
     * Persistence service
     *
     * @param host mico platform address
     * @param user
     * @param password
     */
    /*public PersistenceServiceImpl(String host, String user, String password) throws MalformedURLException {
        this(new URL("http", host, 8080, "/marmotta"), new URL("ftp://" + user + ":" + password + "@" + host));
    }*/

    public PersistenceServiceImpl(java.net.URI marmottaServerUrl, java.net.URI contentUrl) {
        this.marmottaServerUrl = marmottaServerUrl;
        this.contentUrl        = contentUrl;

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

    private java.net.URI getContext() {
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

        UUID id = UUID.randomUUID();

        ContentItem ci = new MarmottaContentItem(marmottaServerUrl,contentUrl,id.toString());

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createContentItem", of("g", marmottaServerUrl.toString(), "ci", ci.getURI().stringValue())));

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
            m.update(createNamed("createContentItem", of("g", marmottaServerUrl.toString(), "ci", ci.getURI().stringValue())));

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
            if(m.ask(createNamed("askContentItem", of("g", marmottaServerUrl.toString(), "ci", id.stringValue())))) {
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
            m.update(createNamed("deleteContentItem", of("g", marmottaServerUrl.toString(), "ci", id.stringValue())));

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
            final TupleQueryResult r = m.query(createNamed("listContentItems", of("g", marmottaServerUrl.toString())));


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
