package com.github.fschlimper.modttxmailforwarderlib;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.fschlimper.modttxmailforwarderlib.FolderObserver;
import com.github.fschlimper.modttxmailforwarderlib.MailConfiguration;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class FolderObserverTest {

	private GreenMail greenMail;
	
	@Before
	public void setupGreenmailServer() throws Exception {
		greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);
		GreenMailUser user = greenMail.setUser("module2@modttx.org", "password");
		greenMail.getManagers().getImapHostManager().createMailbox(user, "Gesendete Objekte");
		greenMail.start();
	}
	
	@After
	public void shutDownGreenmailServer() {
		greenMail.stop();
	}
	
	@Test
	public void testForwarding2() throws Exception {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", 3025); 
    	props.put("mail.smtp.starttls.enable", "false");
    	props.put("mail.smtp.auth", "false");
    	props.put("mail.smtp.host", "localhost");    	
		Session session = Session.getDefaultInstance(props);
		
		MailConfiguration observedAccountConfiguration = new MailConfiguration("module2@modttx.org", "password", "localhost", 3025, false, "localhost", 3143, false);
		FolderObserver folderObserver = new FolderObserver(session, observedAccountConfiguration, "Gesendete Objekte", "excon@modttx.org", new MailConfiguration());
		
		Message message = GreenMailUtil.createTextEmail("module1@modttx.org", "module2@modttx.org", "Subject", "Hello World!", ServerSetupTest.SMTP_IMAP[0]);
		
		folderObserver.messagesReceived(new Message[] {message});
		MimeMultipart content = (MimeMultipart)greenMail.getReceivedMessages()[0].getContent();
		assertEquals(1, content.getCount());
		assertEquals("Hello World!", ((MimeMessage) content.getBodyPart(0).getContent()).getContent());
		
	}
	
}
