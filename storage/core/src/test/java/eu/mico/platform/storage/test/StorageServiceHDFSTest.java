package eu.mico.platform.storage.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.storage.impl.StorageServiceHDFS;


/*
 * Parameterized test class, testing the HDFS storage service against several hosts
 */

@RunWith(Parameterized.class)
public class StorageServiceHDFSTest {

	private String host;
	private boolean isHostReachable;
	private URI contentId;
	
	final static String testString = "This is a Test.";
	
	private static Logger log = LoggerFactory.getLogger(StorageServiceHDFSTest.class);
	
	@Parameters
	public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { "localhost" }, { "10.129.8.152" } , {"10.129.0.15"} 
           });
    }
	
	public StorageServiceHDFSTest(String currTestHost) throws URISyntaxException {
		
		this.host = currTestHost;
		this.isHostReachable = isRemoteHostReachable();
		this.contentId = new URI("junittest");
	}
	
	

	//-------------   Unit tests start here --------------// 
	
	
	
	@Test 
	public void canWriteContent(){
		
		Assume.assumeTrue(isHostReachable);
        StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
        try{
        	OutputStream out = s.getOutputStream(contentId);
	        IOUtils.copy(new ByteArrayInputStream(testString.getBytes()), out);
			out.close();
			
	        final FileSystem fs = getHdfsHostedAt(host);
	        final Path file =  getPathForFile(contentId);
	        Assert.assertTrue(fs.exists(file));
	        Assert.assertTrue(fs.isFile(file));
	        fs.close();
	        
        }
        //the throw, if happening, is raised by the OutputStream::close function -> no need to double-check its state
        catch(IOException e) {
        	log.error("Unexpected exception");
        	Assert.fail();
        }
        
	}
	
	@Test
	public void cannotReadFromNotExistingContent() throws URISyntaxException{
		
		Assume.assumeTrue(isHostReachable);
		StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
        boolean raisedIOException = false;
        URI invalidURI = new URI(UUID.randomUUID().toString());
        try{
        	
			InputStream in = s.getInputStream(invalidURI);
        	
        	//unreachable, the stream is never open
        	in.close();
        }
        catch(IOException e) {
        	raisedIOException = true;
        }
        Assert.assertTrue("The READ request for non-existing content " + invalidURI + " should have failed",raisedIOException);
        
	}
	
	@Test
	public void canReadExistingContent() throws URISyntaxException{
		
		Assume.assumeTrue(isHostReachable);

		//generate the content that we need
		canWriteContent();
		
		StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
		
		//and then verify that we are able to retrieve it
        try{
        	
			InputStream in = s.getInputStream(contentId);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
	        Assert.assertEquals(testString, new String(out.toByteArray()));
	        
	        in.close();
	    }
        //the throw, if happening, is raised by the close() function -> no need to double-check its state
        catch(IOException e) {
        	log.error("Unexpected exception",e);
        	Assert.fail();
        }
        
	}
	
	@Test
	public void canDeleteExistingContent() throws URISyntaxException, IOException{
		
		Assume.assumeTrue(isHostReachable);

		//generate the content that we need
		canWriteContent();
		
		StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
    	Assert.assertTrue(
    		"The DELETE request for existing content '" + contentId + "' failed",
    		s.delete(contentId)
    	);
    	
        final FileSystem fs = getHdfsHostedAt(host);
        final Path file =  getPathForFile(contentId);
        Assert.assertFalse(fs.exists(file));
        fs.close();
        
	}
	
	@Test
	public void cannotDeleteNonExistingContent() throws URISyntaxException{
		
		Assume.assumeTrue(isHostReachable);

		StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
        URI invalidURI = new URI(UUID.randomUUID().toString());
        try{
        	Assert.assertFalse(
        		"The DELETE request for non-existing content " + invalidURI + " should have failed",
        		s.delete(invalidURI)
        	);
        }
        catch(IOException e) {
        	log.error("Unexpected exception",e);
        	Assert.fail();
        }
	}
	
	

	//After each unit test, cleanup the file system from the test file (if it was created)
	@After
    public void removeTestFile() throws IOException
    {
        if(isHostReachable) {
	        final FileSystem fs = getHdfsHostedAt(host);
	        final Path file =  getPathForFile(contentId);
	        if (fs.exists(file) && fs.isFile(file)) {
	            fs.delete(file, false);
	        }
	        fs.close();
	    }
    }
	
	
	//utils to check if the remote test host is reachable
	private boolean isRemoteHostReachable() {
        try {
            final FileSystem fs = getHdfsHostedAt(host);
            fs.getStatus();
            fs.close();
            log.info("Performing test for HDFS host '{}'",host);
            return true;
        } catch (Exception ex) {
        	log.warn("The HDFS host '{}' is not reachable",host);
            return false;
        }
    }
	
	private FileSystem getHdfsHostedAt(String host) throws IOException {
        Configuration hdfsConfig = new Configuration();
        
        hdfsConfig.set("fs.defaultFS", "hdfs://" + host);
        hdfsConfig.set("dfs.client.use.datanode.hostname", "true");
        hdfsConfig.set("dfs.client.read.shortcircuit", "false");
        
        return FileSystem.get(hdfsConfig);
    }
	
	private Path getPathForFile(URI part) {
		return new Path(null, null, "/" + part.getPath() + ".bin");
	}
}
