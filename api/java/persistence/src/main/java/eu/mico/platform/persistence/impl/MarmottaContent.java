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
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Selector;
import com.github.anno4j.model.impl.target.SpecificResource;
import com.google.common.base.Preconditions;
import eu.mico.platform.persistence.metadata.MICOProvenance;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceBuilder;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContent implements Content {

    private static Logger log = LoggerFactory.getLogger(MarmottaContent.class);

    private MarmottaContentItem item;

    private java.net.URI marmottaBase;
    private StorageService storage;
    private String id;


    public MarmottaContent(MarmottaContentItem item, java.net.URI marmottaBase, java.net.URI storageHost, String id) {
        this.item       = item;
        this.marmottaBase = marmottaBase;
        this.id = id;
        this.storage = StorageServiceBuilder.buildStorageService(storageHost);
    }


    protected MarmottaContent(MarmottaContentItem item, java.net.URI marmottaBase, java.net.URI storageHost, URI uri) {
        Preconditions.checkArgument(URITools.validBaseURI(uri.stringValue(), marmottaBase.toString()), "The content part URI \"" + uri.stringValue() + "\" must match the marmottaBase \"" + marmottaBase.toString() + "\"");
        this.item       = item;
        this.marmottaBase = marmottaBase;
        Preconditions.checkArgument(URITools.validContentPartURI(uri.stringValue(), marmottaBase.toString()), "The given content part URI \"" + uri.stringValue() + "\" does not address a content part");
        this.id = URITools.getContentPartID(uri.stringValue(), marmottaBase.toString());
        this.storage = StorageServiceBuilder.buildStorageService(storageHost);
    }

    @Override
    public String getID() {
        return id;
    }

    /**
     * Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     * that it derives from the ContentItem this part belongs to.
     *
     * @return
     */
    @Override
    public URI getURI() {
        return new URIImpl(marmottaBase.toString() + "/" + item.getID() + "/" + id);
    }


    /**
     * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     *
     * @param type
     */
    @Override
    public void setType(String type) throws RepositoryException {
        Metadata m = item.getMetadata();

        try {
            m.update(createNamed("setContentType", of("ci", item.getURI().stringValue(), "cp", getURI().stringValue(), "type", type)));
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        }
    }

    /**
     * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     */
    @Override
    public String getType() throws RepositoryException {
        Metadata m = item.getMetadata();
        try {
            TupleQueryResult r = m.query(createNamed("getContentType", "ci", item.getURI().stringValue(), "cp", getURI().stringValue()));
            if (r.hasNext()) {
                return r.next().getValue("t").stringValue();
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed", e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed", e);
        }
    }



    /**
     * Set the property value for the given property of this content part using an arbitrary string identifier;
     *
     * @param value
     */
    @Override
    public void setProperty(URI property, String value) throws RepositoryException {
        Metadata m = item.getMetadata();

        try {
            m.update(createNamed("setContentProperty", of("ci", item.getURI().stringValue(), "p", property.stringValue(), "cp", getURI().stringValue(), "value", value)));
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        }
    }

    /**
     * Return the property value of this content part for the given property.
     */
    @Override
    public String getProperty(URI property) throws RepositoryException {
        Metadata m = item.getMetadata();
        try {
            TupleQueryResult r = m.query(createNamed("getContentProperty", "ci", item.getURI().stringValue(), "p", property.stringValue(), "cp", getURI().stringValue()));
            if (r.hasNext()) {
                return r.next().getValue("t").stringValue();
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed", e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed", e);
        }
    }

    @Override
    public void addMetadata(Model metadata) throws RepositoryException {
        item.getMetadata().load(metadata);
    }

    /**
     * Set the property relation for the given property of this content part using another resource.
     *
     * @param value
     */
    @Override
    public void setRelation(URI property, URI value) throws RepositoryException {
        Metadata m = item.getMetadata();

        try {
            m.update(createNamed("setContentProperty", of("ci", item.getURI().stringValue(), "p", property.stringValue(), "cp", getURI().stringValue(), "value", value.stringValue())));
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Return the property value of this content part for the given property.
     */
    @Override
    public Value getRelation(URI property) throws RepositoryException {
        Metadata m = item.getMetadata();
        try {
            TupleQueryResult r = m.query(createNamed("getContentProperty", "ci", item.getURI().stringValue(), "p", property.stringValue(), "cp", getURI().stringValue()));
            if(r.hasNext()) {
                return r.next().getValue("t");
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        }
    }


    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     *
     * @return
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            return storage.getOutputStream(new java.net.URI(item.getID() + "/" + getID()));
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }

    /**
     * Return a new input stream for reading the content.
     *
     * @return
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return storage.getInputStream(new java.net.URI(item.getID() + "/" + getID()));
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }

    @Override
    public ContentItem getContentItem() {
        return item;
    }

    @Override
    public Annotation createAnnotation(Body body, Content source, MICOProvenance provenance, Selector selection) throws RepositoryException {

        Annotation annotation = new Annotation();
        annotation.setBody(body);

        if(source != null) {
            SpecificResource specificResource = new SpecificResource();
            specificResource.setSelector(selection);
            specificResource.setSource(new ContentPartRMO(new URIImpl(source.getURI().toString())));
            annotation.setTarget(specificResource);
        }

        // setting the current timestamp
        annotation.setAnnotatedAt(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString());
        annotation.setAnnotatedBy(new MICOSoftwareAgent(provenance.getRequires(), provenance.getProvides(), provenance.getExtractorName()));

        // Write the annotation object to the triple store
        Anno4j.getInstance().createPersistenceService().persistAnnotation(annotation);

        // Link content part to annotation
        Metadata m = item.getMetadata();
        try {
            m.update(
                    createNamed("createAnnotation",
                            of(
                                    "cp", getURI().stringValue(),
                                    "an", annotation.getResource().toString()
                            )
                    )
            );
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed", e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed", e);
        }

        return annotation;
    }

    @Override
    public Annotation createAnnotation(Body body, Content source, MICOProvenance provenance) throws  RepositoryException {
        return createAnnotation(body, source, provenance, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarmottaContent that = (MarmottaContent) o;

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

    @Override
    public String toString() {
        return "ContextualMarmottaContent{" +
                "marmottaBase='" + marmottaBase + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
