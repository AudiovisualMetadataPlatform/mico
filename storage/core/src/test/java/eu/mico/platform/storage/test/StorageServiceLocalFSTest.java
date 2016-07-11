package eu.mico.platform.storage.test;

import eu.mico.platform.storage.impl.StorageServiceLocalFS;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 *
 * @author Horst Stadler (horst.stadler@salzburgresearch.at)
 */

public class StorageServiceLocalFSTest {

    final static byte[] fileContents = "This is a test.".getBytes();
    static URI basePath;
    static String contentPart1;
    static String contentPart2;

    @BeforeClass
    public static void init() {
        basePath = Paths.get(System.getProperty("java.io.tmpdir")).toUri();
        final String contentItem = UUID.randomUUID().toString();
        contentPart1 = contentItem + "/file1";
        contentPart2 = contentItem + "/file2";
    }

    @Test
    public void testCreateReadDeleteFile() throws IOException, URISyntaxException
    {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);

        OutputStream fileOut = storage.getOutputStream(new URI(contentPart1));
        Assert.assertNotNull(fileOut);
        fileOut.write(fileContents);
        fileOut.close();
        Assert.assertTrue(Paths.get(basePath).resolve(contentPart1 + ".bin").toFile().exists());

        InputStream fileIn = storage.getInputStream(new URI(contentPart1));
        Assert.assertNotNull(fileIn);
        byte[] data = new byte[fileContents.length];
        Assert.assertEquals(fileIn.read(data), fileContents.length);
        Assert.assertArrayEquals(fileContents, data);
        Assert.assertEquals(fileIn.read(), -1);
        fileIn.close();

        Assert.assertTrue(storage.delete(new URI(contentPart1)));
        Assert.assertFalse(Paths.get(basePath).resolve(contentPart1 + ".bin").toFile().exists());

        Assert.assertFalse(storage.delete(new URI(contentPart1)));
   }

    @Test
    public void testBasePathEscaping() throws IOException, URISyntaxException {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);
        Assert.assertNull(storage.getOutputStream(new URI("../../test")));
    }

    @Test
    public void testDirectoryName() throws IOException, URISyntaxException {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);
        Assert.assertNull(storage.getOutputStream(new URI("test/")));
    }

    @Test
    public void testWindowsPath() throws Exception {

        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);
        URI winPath = URI.create("file:/X:/workspace/bla");
        Assert.assertNull(storage.getOutputStream(winPath));
    }


    @Test
    public void testRecursiveDelete() throws IOException, URISyntaxException {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);

        OutputStream fileOut1 = storage.getOutputStream(new URI(contentPart1));
        Assert.assertNotNull(fileOut1);
        fileOut1.close();
        OutputStream fileOut2 = storage.getOutputStream(new URI(contentPart2));
        Assert.assertNotNull(fileOut2);
        fileOut2.close();

        Assert.assertTrue(storage.delete(new URI(contentPart1)));
        Assert.assertFalse(Paths.get(basePath).resolve(contentPart1 + ".bin").toFile().exists());
        Assert.assertTrue(Paths.get(basePath).resolve(contentPart2 + ".bin").toFile().exists());

        Assert.assertTrue(storage.delete(new URI(contentPart2)));
        Assert.assertFalse(Paths.get(basePath).resolve(contentPart2 + ".bin").getParent().toFile().exists());
    }

    @Test
    public void testAbsoluteContentPart() throws IOException, URISyntaxException {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(basePath);

        OutputStream fileOut = storage.getOutputStream(new URI("/" + contentPart1));
        Assert.assertNotNull(fileOut);
        fileOut.close();

        Assert.assertTrue(storage.delete(new URI("/" + contentPart1)));
    }

    @Test(expected = java.nio.file.FileSystemNotFoundException.class)
    public  void testInvalidScheme() throws URISyntaxException {
        StorageServiceLocalFS storage = new StorageServiceLocalFS(new URI("http://www.mico-project.eu/test"));
    }
}
