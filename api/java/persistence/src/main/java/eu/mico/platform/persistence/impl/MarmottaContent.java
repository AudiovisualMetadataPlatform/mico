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
import eu.mico.platform.persistence.exception.ConceptNotFoundException;
import eu.mico.platform.persistence.metadata.IBody;
import eu.mico.platform.persistence.metadata.IProvenance;
import eu.mico.platform.persistence.metadata.ISelection;
import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceBuilder;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContent implements Content {

    private static Logger log = LoggerFactory.getLogger(MarmottaContent.class);

    private final String CONCEPT_PATH = "META-INF/org.openrdf.concepts";

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
    public AnnotationImpl createAnnotation(IBody body, Content source, IProvenance provenance, ISelection selection) throws RepositoryException {

        /*
         * Check if the extractor has created the org.openrdf.concepts file. Alibaba requires this file (can be empty), 
         * to persist the annotated objects. If the file was not found, a ConceptNotFoundException will be thrown.
         */
        if (!new File(body.getClass().getClassLoader().getResource(CONCEPT_PATH).getFile()).isFile()) {
            throw new ConceptNotFoundException("Please create an empty org.openrdf.conpepts file inside your META-INF folder.");
        }

        // create annotation object
        AnnotationImpl annotation = new AnnotationImpl();

        // add body, selection and content to the annotation object
        annotation.setBody(body);

        TargetImpl target = new TargetImpl();
        target.setSelection(selection);
        target.setSource(source.getURI().toString());

        annotation.setTarget(target);

        // Setting the provenance information
        annotation.setProvenance(provenance);

        ObjectConnection con = null;

        try {
            // get the repository
            Repository store = item.getMetadata().getRepository();

            // wrap in an object repository
            ObjectRepositoryFactory factory = new ObjectRepositoryFactory();

            ObjectRepository repository = factory.createRepository(store);
            con = repository.getConnection();

            // add the annotation to the repository
            con.addObject(annotation);
        } catch (RepositoryConfigException e) {
            log.error("Unable to create the repository: ", e);
            throw new RepositoryException("Unable to create the repository: ", e);
        }


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

        // close connection
        con.close();

        return annotation;
    }

    @Override
    public AnnotationImpl createAnnotation(IBody body, Content source, IProvenance provenance) throws ConceptNotFoundException, RepositoryException {
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
