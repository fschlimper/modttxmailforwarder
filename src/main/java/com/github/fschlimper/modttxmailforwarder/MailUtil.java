package com.github.fschlimper.modttxmailforwarder;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {

	private Session session;
	
	public MailUtil(Session session) {
		this.session = session;
	}
	
	public void forwardMail(Message message, String recipient, String username, String password) throws AddressException, MessagingException {
		Message forward = new MimeMessage(session);
        // Fill in header
        forward.setRecipients(Message.RecipientType.TO, new Address[] {new InternetAddress(recipient)} );
        forward.setSubject("Fwd: [" + InternetAddress.parse(InternetAddress.toString(message.getFrom())) + " -> " +  InternetAddress.parse(InternetAddress.toString(message.getAllRecipients())) + "]: " + message.getSubject());
        forward.setFrom(new InternetAddress("mail-forwarder@modttx.org"));

        // Create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        // Create a multipart message
        Multipart multipart = new MimeMultipart();
        // set content
        messageBodyPart.setContent(message, "message/rfc822");
        // Add part to multi part
        multipart.addBodyPart(messageBodyPart);
        // Associate multi-part with message
        forward.setContent(multipart);
        //forward.saveChanges();

        // Send the message by authenticating the SMTP server
        // Create a Transport instance and call the sendMessage
        Transport t = session.getTransport("smtp");
        try {
           //connect to the smpt server using transport instance
	  //change the user and password accordingly
           t.connect(username, password);
           t.sendMessage(forward, forward.getAllRecipients());
        } finally {
           t.close();
        }
	}
	
}
