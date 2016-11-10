/*
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

package eu.mico.platform.demo;

import eu.mico.platform.persistence.model.Item;
import org.apache.commons.mail.EmailException;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 19.11.15.
 */
public class EmailThread extends Thread {

    public static final long timeout = 1200000;
    public static final long timestep = 30000;

    private String email;
    private Item item;
    private long start;
    private String ciName;

    public EmailThread(String email, String ciName, Item item) {
        super();
        this.email = email;
        this.item = item;
        this.ciName = ciName;
        this.start = System.currentTimeMillis();
    }

    public void run() {
        try {
            EmailService.sendEmail(email, "Analysis started", "Hi!\n\nThanks for testing MICO Analysis. As soon as the analysis for '%s' has been finished, we will inform you via email!\n\nYour MICO team", ciName);

            while(start + timeout > System.currentTimeMillis()) {

                // FIXME: Update this to use the broker REST interface to get the processing state
/*                ItemState state = broker.getStates().get(item.getURI().stringValue());
                if(state != null && state.isFinalState()) {
                    EmailService.sendEmail(email, "Analysis finished", "Hi!\n\nThe analysis for '%s' has been finished. You can get your results here:\n\nhttp://demo3.mico-project.eu/#/video?uri=%s\n\nYour MICO team", ciName, item.getURI().stringValue());
                    return;
                }*/
                sleep(timestep);
            }

            EmailService.sendEmail(email, "Analysis error", "Hi!\n\nWe are sorry, but the analysis for '%s' run into a timeout. Please try another video!\n\nYour MICO team", ciName);

        } catch (EmailException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
