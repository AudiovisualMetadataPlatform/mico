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
package eu.mico.platform.persistence.model;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Selector;
import com.github.anno4j.model.Target;
import eu.mico.platform.persistence.metadata.MICOProvenance;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
 * content.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * @author Andreas Eisenkolb
 * @author Horst Stadler
 */
public interface Part extends Resource {

    /**
     * Return the internal id
     *
     * @return
     */
    String getID();

    /**
     *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     *  that it derives from the Item this part belongs to.
     * @return
     */
    URI getURI();

    /**
     * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     *
     * @param type
     */
    void setType(String type) throws RepositoryException;

    /**
     * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     */
    String getType() throws RepositoryException;

    /**
     * Set the property value for the given property of this content part using an arbitrary string identifier;
     *
     * @param value
     */
    void setProperty(URI property, String value) throws RepositoryException;

    /**
     * Return the property value of this content part for the given property.
     */
    String getProperty(URI property) throws RepositoryException;

    /**
     * Add metadata in a single batch
     *
     * @param metadata
     * @throws RepositoryException
     */
    void addMetadata(Model metadata) throws RepositoryException;

    /**
     * Set the property relation for the given property of this content part using another resource.
     *
     * @param value
     */
    void setRelation(URI property, URI value) throws RepositoryException;

    /**
     * Return the property value of this content part for the given property.
     */
    Value getRelation(URI property) throws RepositoryException;

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     * @return
     */
    OutputStream getOutputStream() throws IOException;

    /**
     *  Return a new input stream for reading the content.
     * @return
     */
    InputStream getInputStream() throws IOException;

    /**
     * Return the parent content item.
     * @return
     */
    Item getContentItem();

    /**
     * Creates and persists the annotation object for the content part.
     * @return the created annotation object
    */
    Annotation createAnnotation(Body body, Part source, MICOProvenance provenance, Selector selection) throws RepositoryException;
    /**
     * Creates and persists the annotation object for the content part.
     * @return the created annotation object
     */
    Annotation createAnnotation(Body body, Part source, MICOProvenance provenance) throws RepositoryException;

    /**
     * Creates the annotation object for the first content part of a content item.
     * @return the created annotation object
     */
    Annotation createAnnotation(Body body, Part source, MICOProvenance provenance, Target target) throws RepositoryException;

    /**
     * @return Returns all annotation objects that have a reference to the given content part.
     * @throws RepositoryException
     */
    List<Annotation> findDerivedAnnotations() throws RepositoryException;
}
