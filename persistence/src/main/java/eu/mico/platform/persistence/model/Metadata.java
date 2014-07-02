package eu.mico.platform.persistence.model;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ContentPart Metadata, in RDF format. Offers structured access through the Sesame Repository API (and later commons-rdf
 * once it becomes available)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface Metadata {

    /**
     * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
     *
     * @param in      InputStream to load the data from
     * @param format  data format the RDF data is using (e.g. Turtle)
     */
    public void load(InputStream in, RDFFormat format);


    /**
     * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
     * format. Can be used for exporting the metadata.
     *
     * @param out    OutputStream to export the data to
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    public void dump(OutputStream out, RDFFormat format);


    /**
     * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
     * used for any kind of modification of the data.
     *
     * @param sparqlUpdate
     */
    public void update(String sparqlUpdate);


    /**
     * Execute a SPARQL query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    public TupleQueryResult query(String sparqlQuery);
}
