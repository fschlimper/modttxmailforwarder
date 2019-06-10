package com.github.fschlimper.modttxmailforwarder;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.mail.imap.IMAPFolder;

public class FolderObserver implements Runnable {

	private static final Logger logger = LogManager.getLogger();
	
	private Session session;
	private MailConfiguration mailForwarderConfig;
	private MailConfiguration observedAccountConfig;
	
	private Store store = null;
	private IMAPFolder folder;
	private String recipient;
	
	public FolderObserver(Session session, MailConfiguration observedAccountConfig, String foldername, String recipient, MailConfiguration mailForwarderConfig) {
		this.session = session;
		this.observedAccountConfig = observedAccountConfig;
		this.recipient = recipient;
		this.mailForwarderConfig = mailForwarderConfig;
		
		try {
			store = session.getStore(observedAccountConfig.isUseSslForImap() ? "imaps" : "imap");
		
			store.connect(observedAccountConfig.getImapHost(), observedAccountConfig.getImapPort(), observedAccountConfig.getUser(), observedAccountConfig.getPassword());
			
			logger.info("'" + store.getURLName() + "' connected");
			
			folder = (IMAPFolder) store.getFolder(foldername);
			folder.open(Folder.READ_ONLY);
			
			folder.addMessageCountListener(new MessageCountListener() {
				
				@Override
				public void messagesRemoved(MessageCountEvent e) {
					// Nothing to do here
				}
				
				@Override
				public void messagesAdded(MessageCountEvent e) {
					messagesReceived(e.getMessages());
				}
			});
		
		} catch (MessagingException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				folder.idle();
			}
		} catch (MessagingException e) {
			logger.error(e);
		} finally {
			closeConnection();
		}
	}
	
	protected void messagesReceived(Message[] messages) {
		for (Message message: messages) {
			String subject = "Unknown subject";
			try {
				subject = message.getSubject();
				forwardMail(message, recipient, mailForwarderConfig.getUser(), mailForwarderConfig.getPassword());
			} catch (MessagingException e) {
				logger.error(e);
			}
			logger.info("[" + folder.getStore().getURLName() + "] Message received: " + subject);
		}
	}
	
	protected void forwardMail(Message message, String recipient, String username, String password) throws AddressException, MessagingException {
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
	
	protected void closeConnection() {
		try {
			if (folder != null) {
				folder.close(false);
			}
			if (store != null) {
				store.close();
			}
		} catch (MessagingException me) {}
		logger.info("'" + observedAccountConfig.getUser() + "' closed");
	}
	
}
