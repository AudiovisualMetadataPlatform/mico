package eu.mico.platform.broker.service;

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
