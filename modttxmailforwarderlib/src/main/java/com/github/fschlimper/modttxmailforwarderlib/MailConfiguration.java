package com.github.fschlimper.modttxmailforwarderlib;

public class MailConfiguration {

	private String user;
	private String password;
	private String smtpHost;
	private int smtpPort;
	private boolean useSslForSmtp;
	private String imapHost;
	private int imapPort;
	private boolean useSslForImap;
	
	public MailConfiguration() {}
	
	public MailConfiguration(String user, String password, String smtpHost, int smtpPort, boolean useSslForSmtp, 
			String imapHost, int imapPort, boolean useSslForImap) {
		this.user = user;
		this.password = password;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.useSslForSmtp = useSslForSmtp;
		this.imapHost = imapHost;
		this.imapPort = imapPort;
		this.useSslForImap = useSslForImap;
	}

	@Override
	public boolean equals(Object obj) {
		MailConfiguration other = (MailConfiguration) obj;
		
		return other != null
				&& smtpHost != null && smtpHost.equals(other.smtpHost) || smtpHost == other.smtpHost
				&& smtpPort == other.smtpPort
				&& useSslForSmtp == other.useSslForSmtp
				&& imapHost != null && imapHost.equals(other.imapHost) || imapHost == other.imapHost
				&& imapPort == other.imapPort
				&& useSslForImap == other.useSslForImap
				&& user != null && user.equals(other.user) || user == other.user
				&& password != null && password.equals(other.password) || password == other.password;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public int getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	public boolean isUseSslForSmtp() {
		return useSslForSmtp;
	}
	public void setUseSslForSmtp(boolean useSslForSmtp) {
		this.useSslForSmtp = useSslForSmtp;
	}
	public String getImapHost() {
		return imapHost;
	}
	public void setImapHost(String imapHost) {
		this.imapHost = imapHost;
	}
	public int getImapPort() {
		return imapPort;
	}
	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}
	public boolean isUseSslForImap() {
		return useSslForImap;
	}
	public void setUseSslForImap(boolean useSslForImap) {
		this.useSslForImap = useSslForImap;
	}
	
	
}
