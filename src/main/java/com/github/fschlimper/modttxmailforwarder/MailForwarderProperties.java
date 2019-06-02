package com.github.fschlimper.modttxmailforwarder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MailForwarderProperties {

	private static final String USER_PROP = "${PREFIX}.user";
	private static final String PASSWORD_PROP = "${PREFIX}.password";
	
	private static final String SMTP_HOST_PROP = "${PREFIX}.smtp.host";
	private static final String SMTP_PORT_PROP = "${PREFIX}.smtp.port";
	private static final String SMTP_USESSL_PROP = "${PREFIX}.smtp.usessl";

	private static final String IMAP_HOST_PROP = "${PREFIX}.imap.host";
	private static final String IMAP_PORT_PROP = "${PREFIX}.imap.port";
	private static final String IMAP_USESSL_PROP = "${PREFIX}.imap.usessl";

	private static final String DEFAULT_PREFIX = "default";
	private static final String MAILFORWARDER_PREFIX = "mail-forwarder";
	private static final Pattern OBSERVED_PATTERN = Pattern.compile("(observed\\d{3})");
	
	// The receiver account
	private final static String RECEIVER_EMAIL_PROP = "receiver.email";
	
	private MailConfiguration mailForwarderConfig;
	private String receiverEmail;
	private List<MailConfiguration> observedAccountsConfig = new ArrayList<>();
	
	private Properties props;
	
	public MailForwarderProperties(InputStream is) throws IOException {
		props = new Properties();
		props.load(is);
	
		receiverEmail = props.getProperty(RECEIVER_EMAIL_PROP);
		mailForwarderConfig = createMailConfiguration(MAILFORWARDER_PREFIX);
		parseObservedPrefixes().forEach(prefix -> observedAccountsConfig.add(createMailConfiguration(prefix)));
	}

	protected List<String> parseObservedPrefixes() {
		return props
				.keySet()
				.stream()
				.filter(key -> OBSERVED_PATTERN.matcher((String) key).find())
				.map(key -> ((String) key).substring(0, ((String) key).indexOf('.')))
				.distinct()
				.collect(Collectors.toList());
	}
	
	protected MailConfiguration createDefaultConfig() {
		MailConfiguration result = new MailConfiguration();
		
		result.setSmtpHost(props.getProperty(SMTP_HOST_PROP.replace("${PREFIX}", DEFAULT_PREFIX)));
		result.setSmtpPort(Integer.parseInt(props.getProperty(SMTP_PORT_PROP.replace("${PREFIX}", DEFAULT_PREFIX))));
		result.setUseSslForSmtp(Boolean.parseBoolean(props.getProperty(SMTP_USESSL_PROP.replace("${PREFIX}", DEFAULT_PREFIX))));
		
		result.setImapHost(props.getProperty(IMAP_HOST_PROP.replace("${PREFIX}", DEFAULT_PREFIX)));
		result.setImapPort(Integer.parseInt(props.getProperty(IMAP_PORT_PROP.replace("${PREFIX}", DEFAULT_PREFIX))));
		result.setUseSslForImap(Boolean.parseBoolean(props.getProperty(IMAP_USESSL_PROP.replace("${PREFIX}", DEFAULT_PREFIX))));
		
		return result;
	}
	
	protected MailConfiguration createMailConfiguration(String prefix) {
		MailConfiguration result = createDefaultConfig();
		
		String key = SMTP_HOST_PROP.replace("${PREFIX}", prefix);
		if (props.containsKey(key)) {
			result.setSmtpHost(props.getProperty(key));
		}
		
		key = SMTP_PORT_PROP.replace("${PREFIX}", prefix);
		if (props.containsKey(key)) {
			result.setSmtpPort(Integer.parseInt(props.getProperty(key)));
		}
		
		key = SMTP_USESSL_PROP.replace("${PREFIX}", prefix);
		if (props.containsKey(key)) {
			result.setUseSslForSmtp(Boolean.parseBoolean(props.getProperty(key)));
		}
		
		key = IMAP_HOST_PROP.replace("${PREFIX}", prefix);
		if (props.containsKey(key)) {
			result.setImapHost(props.getProperty(key));
		}
		
		key = IMAP_PORT_PROP.replace("${PREFIX}", prefix);
		if (props.contains(key)) {
			result.setImapPort(Integer.parseInt(props.getProperty(key)));
		}
		
		key = IMAP_USESSL_PROP.replace("${PREFIX}", prefix);
		if (props.contains(key)) {
			result.setUseSslForImap(Boolean.parseBoolean(props.getProperty(key)));
		}
		
		result.setUser(props.getProperty(USER_PROP.replace("${PREFIX}", prefix)));
		result.setPassword(props.getProperty(PASSWORD_PROP.replace("${PREFIX}", prefix)));
		
		return result;
	}
	
	public MailConfiguration getMailForwarderConfig() {
		return mailForwarderConfig;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public List<MailConfiguration> getObservedAccountsConfig() {
		return observedAccountsConfig;
	}
	
	
}
