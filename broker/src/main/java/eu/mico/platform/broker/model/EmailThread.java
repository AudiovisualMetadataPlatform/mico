package eu.mico.platform.broker.model;

import com.github.anno4j.persistence.PersistenceService;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.service.EmailService;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.mail.EmailException;

import javax.ws.rs.core.Response;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 19.11.15.
 */
public class EmailThread extends Thread {

    public static final long timeout = 1200000;
    public static final long timestep = 30000;

    private String email;
    private MICOBroker broker;
    private ContentItem contentItem;
    private long start;
    private String ciName;

    public EmailThread(String email, String ciName, MICOBroker broker, ContentItem contentItem) {
        this.email = email;
        this.broker = broker;
        this.contentItem = contentItem;
        this.ciName = ciName;
        this.start = System.currentTimeMillis();
    }

    public void run() {

        try {
            EmailService.sendEmail(email, "Analysis started", "Hi!\n\nThanks for testing Mico Analysis. As soon as the analysis for '%s' has been finished, we will inform you via email!\n\nYour Mico team", ciName);

            while(start + timeout < System.currentTimeMillis()) {

                ContentItemState state = broker.getStates().get(contentItem.getURI().stringValue());
                if(state != null && state.isFinalState()) {
                    EmailService.sendEmail(email, "Analysis finished", "Hi!\n\nThe analysis for '%s' has been finished. You can get your results here:\n\nhttp://demo3.mico-project.eu/demo/#/video?uri=%s\n\nYour Mico team", ciName, contentItem.getURI().stringValue());
                    return;
                }
                sleep(timestep);
            }

            EmailService.sendEmail(email, "Analysis error", "Hi!\n\nWe are sorry, but the analysis for '%s' run into a timeout. Please try another video!\n\nYour Mico team", ciName);

        } catch (EmailException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
