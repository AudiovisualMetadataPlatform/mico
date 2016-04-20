package eu.mico.platform.storage.test;


import eu.mico.platform.storage.impl.StorageServiceHDFS;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Horst Stadler (horst.stadler@salzburgresearch.at)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StorageServiceHDFSTest {

    final static String host = "localhost";
    static URI contentId = null;
    final static String testString = "This is a Test.";

    static boolean hdfsServerRunning;

    @BeforeClass
    public static void init() throws URISyntaxException
    {
        hdfsServerRunning = isHDFSServerRunning();
        contentId = new URI("junittest");
    }

    @Test
    public void test1ConnectHDFS() throws IOException
    {
        Assume.assumeTrue(isHDFSServerRunning());
        final FileSystem fs = connect();
        fs.getStatus();
        fs.close();
    }

    @Test
    public void test2StoreContent() throws IOException
    {
        Assume.assumeTrue(hdfsServerRunning);
        StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
        PrintStream out = new PrintStream(s.getOutputStream(contentId));
        out.print(testString);
        out.close();
    }

    @Test
    public void test3ContentNaming() throws IOException
    {
        Assume.assumeTrue(hdfsServerRunning);
        final FileSystem fs = connect();
        final Path file =  getContentPartPath(contentId);
        Assert.assertTrue(fs.exists(file));
        Assert.assertTrue(fs.isFile(file));
        fs.close();
    }

    @Test
    public void test4RetrieveContent() throws IOException
    {
        Assume.assumeTrue(hdfsServerRunning);
        StorageServiceHDFS s = new StorageServiceHDFS(host, -1, "/");
        StringWriter writer = new StringWriter();
        InputStream is = s.getInputStream(contentId);
        IOUtils.copy(is, writer);
        is.close();
        Assert.assertEquals(testString, writer.toString());
    }

    @AfterClass
    public static void removeTestFile() throws IOException
    {
        Assume.assumeTrue(hdfsServerRunning);
        final FileSystem fs = connect();
        final Path file =  getContentPartPath(contentId);
        if (fs.exists(file) && fs.isFile(file)) {
            fs.delete(file, false);
        }
        fs.close();
    }

    private static FileSystem connect() throws IOException {
        Configuration hdfsConfig = new Configuration();
        hdfsConfig.set("fs.defaultFS", "hdfs://" + host);
        return FileSystem.get(hdfsConfig);
    }

    private static boolean isHDFSServerRunning() {
        try {
            final FileSystem fs = connect();
            fs.getStatus();
            fs.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Path getContentPartPath(URI part) {
        return new Path(null, null, "/" + part.getPath() + ".bin");
    }


}
