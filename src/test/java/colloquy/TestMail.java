package colloquy;

/**
 * Created by Peter Gershkovich on 2/25/17.
 */

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMail {
    public static void main(String[] args) {


        final String username = "";
        final String password = "";

        Properties props = new Properties();
        props.setProperty("mail.smtp.ssl.enable", "true");
        // set any other needed mail.smtp.* properties here
        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "465");

        
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        // set the message content here


        try
        {

            message.setFrom(new InternetAddress(""));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(""));
            message.setSubject("Testing");
            message.setText("Latest email java client");

            Transport.send(message, username, password);
        } catch (MessagingException e)
        {
            e.printStackTrace();
        }


//        Properties props = new Properties();
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class",
//                "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "465");
//
//        Session session = Session.getDefaultInstance(props,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(username,password);
//                    }
//                });
//
//        try {
//
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(""));
//            message.setRecipients(Message.RecipientType.TO,
//                    InternetAddress.parse(""));
//            message.setSubject("Testing Subject");
//            message.setText("Dear Mail Crawler," +
//                    "\n\n No spam to my email, please!");
//
//            Transport.send(message);
//
//            System.out.println("Done");
//
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
    }
}
