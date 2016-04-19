package eu.mico.platform.api.inject;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.openrdf.repository.RepositoryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

public class Main {

    @Parameter(names = {"--debug", "-d"}, description = "Debug mode")
    private boolean debug = false;

    @Parameter(names={"--host", "-h"}, required = true, description = "Host of the MICO platform")
    private String host;

    @Parameter(names={"--user", "-u"}, required = true, description = "User of the MICO platform")
    private String user;

    @Parameter(names={"--password", "-p"}, required = true, description = "Password for the given user")
    private String pass;

    @Parameter(names={"--assetType", "-at"}, required = true, description = "Type of the asset")
    private String assetType;

    @Parameter(names = {"--asset", "-a"}, converter = FileConverter.class, required = true, description = "Location of the asset binary file")
    private File assetFile;

    public static void main(String ... args) throws RepositoryException, IOException, TimeoutException, URISyntaxException {
        Main main = new Main();
        JCommander jCommander = new JCommander(main, args);
        main.run();

    }

    public void run() throws RepositoryException, IOException, TimeoutException, URISyntaxException {
        // validate asset
        Validate.isTrue(assetFile.exists(), "Asset location doesn't exist");
        Validate.isTrue(assetFile.isFile(), "Asset hat to be a file");

        if(debug) {
            System.out.println("Init connection to mico platform at " + host);
        }
        EventManager eventManager= new EventManagerImpl(host, user, pass);
        eventManager.init();

        if(debug) {
            System.out.println("Get persistence service");
        }
        PersistenceService ps = eventManager.getPersistenceService();;


        if(debug) {
            System.out.println("Create Item");
        }
        Item item = ps.createItem();
        item.setSyntacticalType(assetType);
        item.setSemanticType("application/injection-webservice");

        if(debug) {
            System.out.println("Item created: " + item.getURI());
        }


        if(debug) {
            System.out.println("Create Asset with type " + assetType + ". Uploading from location " + assetFile.getAbsolutePath());
        }

        Asset asset = item.getAsset();
        asset.setFormat(assetType);
        OutputStream out = asset.getOutputStream();
        int bytes = IOUtils.copy(new FileInputStream(assetFile), out);
        out.close();

        System.out.printf("Created Item %s with Asset %s (%s, %d bytes)\n", item.getURI(), asset.getLocation(), asset.getFormat(), bytes);

        // shutdown event manager properly
        eventManager.shutdown();
        System.exit(0);
    }
}
