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

import com.google.common.base.Preconditions;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.persistence.util.IDUtils;
import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * Implementation of a ContentItem based on a backend contextual marmotta SPARQL webservice.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContentItem implements ContentItem {


    public static final String SUFFIX_METADATA = "-metadata";
    public static final String SUFFIX_EXECUTION = "-execution";
    public static final String SUFFIX_RESULT = "-result";


    private static Logger log = LoggerFactory.getLogger(MarmottaContentItem.class);

    private java.net.URI marmottaBase;
    private java.net.URI storageHost;
    private StorageService storage;

    // the content item's unique ID
    private String id;

    public MarmottaContentItem(java.net.URI marmottaBase, java.net.URI storageHost, String id) {
        this.marmottaBase = marmottaBase.normalize();
        this.id = id;
        this.storageHost = storageHost.normalize();
        this.storage = StorageServiceBuilder.buildStorageService(this.storageHost);
    }


    protected MarmottaContentItem(java.net.URI marmottaBase, java.net.URI storageHost, URI uri) {
        Preconditions.checkArgument(URITools.validBaseURI(uri.stringValue(), marmottaBase.toString()), "The content item URI \"" + uri.stringValue() + "\" must match the marmottaBase \"" + marmottaBase.toString() + "\"");
        this.marmottaBase = marmottaBase.normalize();
        this.storageHost = storageHost.normalize();
        this.storage = StorageServiceBuilder.buildStorageService(this.storageHost);
        Preconditions.checkArgument(URITools.validContentItemURI(uri.stringValue(), this.marmottaBase.toString()), "The given content item URI \"" + uri.stringValue() + "\" does not address a content item");
        this.id = URITools.getContentItemID(uri.stringValue(), this.marmottaBase.toString());
    }

    /**
     * Return the unique identifier for this content item. The ID should be built in a way that it is globally
     * unique (e.g. UUID).
     *
     * @return
     */
    @Override
    public String getID() {
        return id;
    }

    /**
     * Return the identifier (a unique URI) for this content item. This URI will be based on the internal
     * ID of the content item in the platform.
     *
     * @return
     */
    @Override
    public URI getURI() {
        return new URIImpl(URITools.normalizeURI(marmottaBase.toString() + "/" + id));
    }

    /**
     * Return content item metadata part of the initial content item, e.g. provenance information etc. Particularly,
     * whenever a new content part is added to the content item, the system will introduce a triple to the metadata
     * relating the content part to the content item using the ldp:contains relation.
     * <p/>
     * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
     * schema
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getMetadata() throws RepositoryException {
       try {
           return new MarmottaMetadata(marmottaBase.toString(), id + SUFFIX_METADATA);
       } catch (java.net.URISyntaxException e) {
           return null;
       }
     }

    /**
     * Return execution plan and metadata (e.g. dependencies, profiling information, execution information). Can be
     * updated by other components to add their execution information.
     * <p/>
     * TODO: could return a specialised Metadata object once we know the schema for execution metadata
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getExecution() throws RepositoryException {
        try {
            return new MarmottaMetadata(marmottaBase.toString(), id + SUFFIX_EXECUTION);
        } catch (java.net.URISyntaxException e) {
            return  null;
        }
    }

    /**
     * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
     * the result with new information. This will hold the final analysis results.
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getResult() throws RepositoryException {
        try {
            return new MarmottaMetadata(marmottaBase.toString(), id + SUFFIX_RESULT);
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }

    /**
     * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * TODO: init file storage
     *
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content createContentPart() throws RepositoryException {
        Content content = new MarmottaContent(this, marmottaBase, storageHost, IDUtils.generatedRandomId());
        persistContentPart(content);
        return content;
    }

    /**
     * Create a new content part with the given ID and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * TODO: init file storage
     *
     * @param id the ID of the content part to create
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    public Content createContentPart(String id) throws RepositoryException {
        Preconditions.checkArgument(URITools.validID(id), "The given ID \"" + id + "\" contains invalid characters");
        Content content = new MarmottaContent(this, marmottaBase, storageHost,id);
        persistContentPart(content);
        return content;
    }

    /**
     * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * TODO: init file storage
     *
     * @param uri the URI of the content part to create
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content createContentPart(URI uri) throws RepositoryException {
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");
        return createContentPart(URITools.getContentPartID(uri.stringValue(), marmottaBase.toString()));
    }

    private void persistContentPart(Content content) throws RepositoryException{
        Metadata m = getMetadata();
        try {
            m.update(
                    createNamed(
                            "createContentPart",
                            of(
                                    "ci", getURI().stringValue(),
                                    "cp", content.getURI().stringValue(),
                                    "date", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(new Date())
                            )
                    )
            );
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        } finally {
            m.close();
        }
    }

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * TODO: init file storage
     *
     * @param uri the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content getContentPart(URI uri) throws RepositoryException {
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");

        // check if part exists
        Metadata m = getMetadata();

        try {
            if(m.ask(createNamed("askContentPart", of("ci", getURI().stringValue(), "cp", uri.stringValue())))) {
                return new MarmottaContent(this, marmottaBase, storageHost, uri);
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:",e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:",e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        } finally {
            m.close();
        }
    }

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * TODO: init file storage
     *
     * @param uri the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    /*@Override
    public Content getContentPart(URI uri) throws RepositoryException {
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");
        return getContentPart(URITools.getContentPartID(uri.stringValue(), marmottaBase.toString()));
    }*/

    /**
     * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
     * nothing.
     *
     * TODO: delete file storage
     *
     * @param uri the URI of the content part to delete
     */
    @Override
    public void deleteContent(URI uri) throws RepositoryException, IOException {
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");
        // delete metadata
        Metadata m = getMetadata();

        try {
            m.update(createNamed("deleteContentPart", of("ci", getURI().stringValue(), "cp", uri.stringValue())));
            storage.delete(new java.net.URI(getID() + "/" + URITools.getContentPartID(uri.stringValue(), marmottaBase.toString())));
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        } catch (java.net.URISyntaxException e) {
            log.error("Failed removing content because of invalid URI: ", e);
        } finally {
            m.close();
        }

    }

    /**
     * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
     * nothing.
     *
     * TODO: delete file storage
     *
     * @param uri the URI of the content part to delete
     */
    /*@Override
    public void deleteContent(URI uri) throws RepositoryException, IOException {
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");
        deleteContent(URITools.getContentPartID(uri.stringValue(), marmottaBase.toString()));
    }*/

    /**
     * Return an iterator over all content parts contained in this content item.
     *
     * @return an iterable that (non-lazily) iterates over the content parts
     */
    @Override
    public Iterable<Content> listContentParts() throws RepositoryException {

        Metadata m = getMetadata();

        try {
            final TupleQueryResult r = m.query(createNamed("listContentParts", "ci", getURI().stringValue()));
            List<Content> result = new ArrayList<Content>();
            while (r.hasNext()) {
                try {
                    result.add(new MarmottaContent(this, marmottaBase, storageHost, (URI) r.next().getValue("p")));
                } catch (QueryEvaluationException e) {
                    result.add(null);
                }
            }
            r.close();

            return result;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        } finally {
            m.close();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarmottaContentItem that = (MarmottaContentItem) o;

        if (!marmottaBase.equals(that.marmottaBase)) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = marmottaBase.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
