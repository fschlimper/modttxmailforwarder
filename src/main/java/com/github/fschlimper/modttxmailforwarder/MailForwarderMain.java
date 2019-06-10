package com.github.fschlimper.modttxmailforwarder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailForwarderMain {

	private static final Logger logger = LogManager.getLogger();
	
	public static void main(String[] args) {
		
		logger.info("Starting...");
		
		File propertyFile = new File("mailforwarder.properties");
		if (args.length  > 0) {
			propertyFile = new File(args[0]);
		}
		try {
			MailForwarderProperties properties = new MailForwarderProperties(new BufferedInputStream(new FileInputStream(propertyFile)));
		
			Properties props = new Properties();
	    	props.put("mail.transport.protocol", "smtp");
	    	props.put("mail.smtp.port", properties.getMailForwarderConfig().getSmtpPort()); 
	    	props.put("mail.smtp.starttls.enable", "true");
	    	props.put("mail.smtp.auth", "true");
	    	props.put("mail.smtp.host", properties.getMailForwarderConfig().getSmtpHost());

			Session session = Session.getDefaultInstance(props);
			
			properties.getObservedAccountsConfig().forEach( mailConfig -> {
				Thread t = new Thread(new FolderObserver(session, mailConfig, "Gesendete Objekte", properties.getReceiverEmail(), properties.getMailForwarderConfig()));
				t.setDaemon(true);
				t.start();
			});
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}

}
