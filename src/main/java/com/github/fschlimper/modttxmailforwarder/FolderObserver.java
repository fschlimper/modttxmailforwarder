package com.github.fschlimper.modttxmailforwarder;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.mail.imap.IMAPFolder;

public class FolderObserver implements Runnable {

	private static final Logger logger = LogManager.getLogger();
	
	private MailUtil mailUtil;
	private String login;
	private String password;
	
	private Store store = null;
	private IMAPFolder folder;
	private String recipient;
	
	public FolderObserver(Session session, MailConfiguration observedAccountConfig, String foldername, String recipient) {
		this.recipient = recipient;
		mailUtil = new MailUtil(session);
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
		while (true) {
			try {
				folder.idle();
			} catch (MessagingException e) {
				logger.error(e);
			} finally {
				closeConnection();
			}
		}
	}
	
	protected void messagesReceived(Message[] messages) {
		for (Message message: messages) {
			String subject = "Unknown subject";
			try {
				subject = message.getSubject();
				mailUtil.forwardMail(message, recipient, login, password);
			} catch (MessagingException e) {
				logger.error(e);
			}
			logger.info("[" + folder.getStore().getURLName() + "] Message received: " + subject);
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
		logger.info("'module1@modttx.org' closed");
	}

	public MailUtil getMailUtil() {
		return mailUtil;
	}

	public void setMailUtil(MailUtil mailUtil) {
		this.mailUtil = mailUtil;
	}

	
	
}
