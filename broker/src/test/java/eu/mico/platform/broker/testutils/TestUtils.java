package eu.mico.platform.broker.testutils;

import java.io.PrintWriter;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;

import com.github.anno4j.model.namespaces.OADM;

import eu.mico.platform.anno4j.model.namespaces.MMM;

public class TestUtils {
    
    public static void debugRDF(final Logger log, RepositoryConnection con) throws RepositoryException {
        if(!log.isDebugEnabled()){
            return;
        }
        //we copy all statements to a TreeModel as this one sorts them by SPO
        //what results in a much nicer TURTLE serialization
        final Model model = new TreeModel();
        //we also set commonly used namespaces
        model.setNamespace(OADM.PREFIX, OADM.NS);
        model.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
        model.setNamespace(RDFS.PREFIX, RDF.NAMESPACE);
        model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNamespace(MMM.PREFIX, MMM.NS);
        model.setNamespace("test", "http://localhost/mem/");
        model.setNamespace("services", "http://www.mico-project.eu/services/");
        boolean active = true;
        try {
            active = con.isActive();
            if(!active){
                con.begin();
            }
            con.exportStatements(null, null, null, true, new RDFHandlerBase(){
                @Override
                public void handleStatement(Statement st) {
                    log.debug("{},{},{},{}",st.getSubject(),st.getPredicate(),st.getObject(),st.getContext());
                    model.add(st);
                }
            });
        } catch (RDFHandlerException e) {
            throw new RuntimeException(e);
        } finally {
            if(!active){
                con.rollback();
            }
        }
        log.debug("--- START generated RDF ---");
        PrintWriter out = new PrintWriter(System.out);
        try {
            Rio.write(model, out, RDFFormat.TURTLE);
        } catch (RDFHandlerException e) {
            throw new RuntimeException(e);
        }
        out.close();
        log.debug("--- END generated RDF ---");
    }
}
