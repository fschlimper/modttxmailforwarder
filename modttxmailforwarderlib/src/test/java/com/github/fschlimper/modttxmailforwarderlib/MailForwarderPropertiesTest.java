package com.github.fschlimper.modttxmailforwarderlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.github.fschlimper.modttxmailforwarderlib.MailConfiguration;
import com.github.fschlimper.modttxmailforwarderlib.MailForwarderProperties;

public class MailForwarderPropertiesTest {

	private final String PROPERTIES = 
			"# Mail accounts\n" + 
			"\n" + 
			"# Default imap/smtp hosts and ports\n" + 
			"# Taken, if nothing ist specified in the mail account\n" + 
			"default.smtp.host		= smtp_host\n" + 
			"default.smtp.port		= 3025\n" + 
			"default.smtp.usessl		= false\n" + 
			"\n" + 
			"default.imap.host		= imap_host\n" + 
			"default.imap.port		= 3143\n" + 
			"default.imap.usessl		= true\n" + 
			"\n" + 
			"# Mail address which ist used as sender for forwarded mails\n" + 
			"mail-forwarder.user				= mail-forwarder@modttx.org\n" + 
			"mail-forwarder.password			= forwarder-password\n" + 
			"\n" + 
			"# The mail configuration for the forward account\n" + 
			"receiver.email					= excon@localhost\n" + 
			"\n" + 
			"# The observed mail accounts\n" + 
			"observed001.user				= module1@modttx.org\n" + 
			"observed001.password			= pass1\n" + 
			"\n" + 
			"observed002.user				= module2@modttx.org\n" + 
			"observed002.password			= pass2\n" + 
			"\n" + 
			"observed003.user				= module3@modttx.org\n" + 
			"observed003.password			= pass3\n" + 
			""
			+ "";
	
	public MailForwarderProperties createMailForwarderProperties() throws IOException {
		return new MailForwarderProperties(new ByteArrayInputStream(PROPERTIES.getBytes()));
	}
	
	public void assertMailConfig(MailConfiguration expected, MailConfiguration actual) {
		assertEquals(expected, actual);
	}
	
	@Test
	public void testDefaultProperties() throws IOException {
		MailForwarderProperties mailForwarderProperties = createMailForwarderProperties();
		
		MailConfiguration actualConfig = mailForwarderProperties.createDefaultConfig();
		MailConfiguration expectedConfig = new MailConfiguration(null, null, "smtp_host", 3025, false, "imap_host", 3143, true);
		
		assertMailConfig(expectedConfig, actualConfig);
	}
	
	@Test
	public void testReceiverEmail() throws IOException {
		MailForwarderProperties mailForwarderProperties = createMailForwarderProperties();
		
		assertEquals("excon@localhost", mailForwarderProperties.getReceiverEmail());
	}
	
	@Test
	public void testForwarderConfig() throws IOException {
		MailForwarderProperties mailForwarderProperties = createMailForwarderProperties();
		
		MailConfiguration actualConfig = mailForwarderProperties.getMailForwarderConfig();
		MailConfiguration expectedConfig = new MailConfiguration("mail-forwarder@modttx.org", "forwarder-password", "smtp_host", 3025, false, "imap_host", 3143, true);
		
		assertMailConfig(expectedConfig, actualConfig);
	}
	
	@Test
	public void testParseObservedPrefixes() throws IOException {
		MailForwarderProperties mailForwarderProperties = createMailForwarderProperties();
		
		List<String> prefixes = mailForwarderProperties.parseObservedPrefixes();
		
		assertEquals(3, prefixes.size());
		assertTrue(prefixes.contains("observed001"));
		assertTrue(prefixes.contains("observed002"));
		assertTrue(prefixes.contains("observed003"));
	}
	
	@Test
	public void testObservedConfigs() throws IOException {
		MailForwarderProperties mailForwarderProperties = createMailForwarderProperties();
		
		List<MailConfiguration> actualConfigs = mailForwarderProperties.getObservedAccountsConfig();
		
		assertEquals(3, actualConfigs.size());

		MailConfiguration expectedConfig = new MailConfiguration("module1@modttx.org", "pass1", "smtp_host", 3025, false, "imap_host", 3143, true);
		assertTrue(actualConfigs.contains(expectedConfig));
		
		expectedConfig = new MailConfiguration("module2@modttx.org", "pass2", "smtp_host", 3025, false, "imap_host", 3143, true);
		assertTrue(actualConfigs.contains(expectedConfig));
		
		expectedConfig = new MailConfiguration("module3@modttx.org", "pass3", "smtp_host", 3025, false, "imap_host", 3143, true);
		assertTrue(actualConfigs.contains(expectedConfig));
	}
}
