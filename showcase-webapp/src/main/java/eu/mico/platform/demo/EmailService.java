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

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 19.11.15.
 */
public class EmailService {

    private static Logger logger = LoggerFactory.getLogger(EmailService.class);

    public static synchronized void sendEmail(String address, String title, String msg, String ... values) throws EmailException {
        String message = String.format(msg,values);
        Email email = new SimpleEmail();
        email.setHostName("smtp.googlemail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("mico.demo.2015@gmail.com", "diggwthtbnwlsatx"));
        email.setSSLOnConnect(true);
        email.setFrom("mico.demo.2015@gmail.com");
        email.setSubject(title);
        email.setMsg(message);
        email.addTo(address);
        email.send();
        logger.info("Sent email '{}' to {}: {}", title, address, message);
    }

}
