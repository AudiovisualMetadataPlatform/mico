package eu.mico.platform.event.test.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.model.Item;

/**
 * Test implementation of the {@link AnalysisResponse} interface that takes care
 * of:<ul>
 * <li> transaction management (commit/rollback calls in the according send**() methods)
 * <li> asserts parameters to send**() methods
 * <li> asserts state send**() methods are called
 * </ul>
 * 
 * @author Rupert Westenthaler
 * @author Marcel Sielang
 * @author Andreas Wisenkolb
 * @author Sergio Fernández
 */
public class AnalysisResponseCollector implements AnalysisResponse {

    private static Logger log = LoggerFactory.getLogger(AnalysisResponseCollector.class);

    private Map<URI, String> progresses;
    private Set<URI> newItems;

    private boolean isFinished;
    private boolean isError;


    public AnalysisResponseCollector() {
        progresses = new HashMap<>();
        newItems = new HashSet<>();
    }

    @Override
    public void sendFinish(Item ci) throws IOException {
        Assert.assertNotNull(ci);
        Assert.assertFalse(isFinished);
        Assert.assertFalse(isError);
        log.debug("sent message about {}", ci.getURI().stringValue());
        isFinished = true;
        ObjectConnection con = ci.getObjectConnection();
        try {
            Assert.assertTrue(con.isActive());
            con.commit();
            //con.close(); //do not close connection in unit tests
        } catch (RepositoryException e) {
            log.warn(e.getMessage(),e);
        }
    }

    public Set<URI> getNewItemResponses() {
        return Collections.unmodifiableSet(newItems);
    }
    
    public Map<URI,String> getProgressResponses() {
        return Collections.unmodifiableMap(progresses);
    }

    @Override
    public void sendProgress(Item ci, URI object, float progress)
            throws IOException {
        Assert.assertNotNull(ci);
        Assert.assertNotNull(object);
        Assert.assertFalse(isFinished);
        Assert.assertFalse(isError);
        ObjectConnection con = ci.getObjectConnection();
        try {
            Assert.assertTrue(con.isActive());
        } catch (RepositoryException e){
            log.warn(e.getMessage(),e);
        }
        log.debug("sent progress message about {}", object.stringValue());
        progresses.put(object, String.valueOf(progress));
    }
    
    @Override
    public void sendError(Item item, AnalysisException e)throws IOException {
        if(e != null){
            sendError(item, e.getCode(), e.getMessage(), e.getCause());
        } else {
            sendError(item,ErrorCodes.UNEXPECTED_ERROR, "", "");
        }
    }

    @Override
    public void sendError(Item item, ErrorCodes code, String msg, Throwable t)
            throws IOException {
        if(t != null){
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            sendError(item,code,msg,writer.toString());
        } else {
            sendError(item,code,msg,"");
        }
    }

    @Override
    public void sendError(Item ci, ErrorCodes code, String msg, String desc)
            throws IOException {
        Assert.assertNotNull(ci);
        Assert.assertNotNull(code);
        Assert.assertFalse(isFinished);
        Assert.assertFalse(isError);
        log.debug("sent error message about {}", ci.getURI().stringValue());
        ObjectConnection con = ci.getObjectConnection();
        isError = true;
        try {
            Assert.assertTrue(con.isActive());
            con.rollback();
            //con.close(); //do not close connection in unit tests
        } catch (RepositoryException e) {
            log.warn(e.getMessage(),e);
        }
    }

    @Override
    public void sendNew(Item ci, URI object)
            throws IOException {
        Assert.assertNotNull(ci);
        Assert.assertNotNull(object);
        Assert.assertFalse(isFinished);
        Assert.assertFalse(isError);
        ObjectConnection con = ci.getObjectConnection();
        Assert.assertTrue("sendNew was called twice for "+ object, newItems.add(object));
        try {
            Assert.assertTrue(con.isActive());
            con.commit();
            con.begin();
        } catch (RepositoryException e) {
            log.warn(e.getMessage(),e);
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public boolean isError() {
        return isError ;
    }

    @Override
    public boolean hasNew() {
        return !newItems.isEmpty();
    }

}
