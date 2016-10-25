package eu.mico.platform.samples.wordcount;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;

import org.apache.commons.cli.*;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

public class WordCountAnalyzerDaemon implements AutoCloseable, Daemon {

    private String username;
    private String password;
    private String hostname;

    private EventManager eventManager;
    private WordCountAnalyzerAnno4j extractor;

    @Override
    public void close() throws Exception {
        System.out.println("WordCountExtractor daemon closed");
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException {
        extractor = new WordCountAnalyzerAnno4j();

        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse(createOptions(), context.getArguments());
            username = cmd.getOptionValue("u");
            password = cmd.getOptionValue("p");
            hostname = cmd.getOptionValue("h");
        } catch (ParseException e) {
            e.printStackTrace();
            throw new DaemonInitException(e.getMessage());
        }

        try {
            eventManager = new EventManagerImpl(hostname, username, password);
            System.out.println("ner daemon initialized");
        } catch (IOException e) {
            e.printStackTrace();
            throw new DaemonInitException(e.getMessage());
        }
        System.out.println("WordCountExtractor daemon initialized");
    }

    @Override
    public void start() throws Exception {
        eventManager.init();
        eventManager.registerService(extractor);
        System.out.println("WordCountExtractor daemon started");
    }

    @Override
    public void stop() throws Exception {
        eventManager.unregisterService(extractor);
        eventManager.shutdown();
        System.out.println("WordCountExtractor daemon stopped");
        close();
    }

    @Override
    public void destroy() {
        extractor = null;
        eventManager = null;
        System.out.println("WordCountExtractor daemon destroyed");
    }

    private static Options createOptions() {
        Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withArgName("username")
                        .hasArg()
                        .withLongOpt("username")
                        .isRequired()
                        .create('u')
        );

        options.addOption(
                OptionBuilder
                        .withArgName("password")
                        .hasArg()
                        .withLongOpt("password")
                        .isRequired()
                        .create('p')
        );

        options.addOption(
                OptionBuilder
                        .withArgName("hostname")
                        .hasArg()
                        .withLongOpt("hostname")
                        .isRequired()
                        .create('h')
        );

        return options;
    }
    
    public static void main(String[] args) {
        if(args.length != 3) {
            System.err.println("Usage: java WordCountExtractor <hostname> <user> <password>");
            System.exit(1);
        }

        String mico_host = args[0];
        String mico_user = args[1];
        String mico_pass = args[2];

        try {
            // create event manager instance, providing the correct host, user and password, and initialise it
            EventManager eventManager = new EventManagerImpl(mico_host,mico_user,mico_pass);
            eventManager.init();

            // create analyzer service instance and register it with event manager
            WordCountAnalyzerAnno4j svc_wc = new WordCountAnalyzerAnno4j();
            eventManager.registerService(svc_wc);


            // keep running service in the background, and wait for user command "q" on the frontent to terminate
            // service (other approaches might be more sensible for a service, e.g. commons-daemon)
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            char c = ' ';
            while(Character.toLowerCase(c) != 'q') {
                System.out.print("enter 'q' to quit: ");
                System.out.flush();

                c = in.readLine().charAt(0);
            }

            // unregister service before quiting
            eventManager.unregisterService(svc_wc);

            // shutdown event manager properly
            eventManager.shutdown();

            System.out.println("WordCountExtractor shutdown completed");
            // DONE
        } catch (IOException e) {
            System.out.println("error while accessing event manager:" + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("invalid hostname:" + e.getMessage());
        } catch (TimeoutException e) {
            System.out.println("fetching configuration timed out:" + e.getMessage());
        }

    }
}

