package com.ostj.utils;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.activation.DataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import org.apache.commons.lang3.StringUtils;


public class EmailSender {
    private static final String DefaultSeparator = ",";

	private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

	private String account;
	private String password;
	private String smtp;
	private String useSSL = "false";
	private String port = "25";
	private String body;
	private String subject;
	private String sender;
	private String addressTo;
	private String addressCC;
	private String addressBCC;

	protected EmailSender(String smtp, String account, String password) {
		this.smtp = smtp;
		this.account = account;
		this.password = password;
	}

	public static EmailSender getEmailSender(String smtp, String account, String password) {
		return new EmailSender(smtp, account, password);
	}

	public EmailSender withTO(String addressTo) {
		this.addressTo = addressTo;
		return this;
	}

	public EmailSender withCC(String addressCC) {
		this.addressCC = addressCC;
		return this;
	}

	public EmailSender withBCC(String addressBCC) {
		this.addressBCC = addressBCC;
		return this;
	}

	protected String getAddressesAsString(List<String> addresses) {
		return getAddressesAsString(addresses, DefaultSeparator);
	}

	protected String getAddressesAsString(List<String> addresses, String separator) {
		if (addresses == null)
			return null;

		return addresses.stream().filter(x -> StringUtils.isNotBlank(x)).collect(Collectors.joining(separator));
	}

	public EmailSender withTO(List<String> addressTo) {
		this.addressTo = getAddressesAsString(addressTo);
		return this;
	}

	public EmailSender withCC(List<String> addressCC) {
		this.addressCC = getAddressesAsString(addressCC);
		return this;
	}

	public EmailSender withBCC(List<String> addressBCC) {
		this.addressBCC = getAddressesAsString(addressBCC);
		return this;
	}

	public EmailSender withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public EmailSender withBody(String body) {
		this.body = body;
		return this;
	}

	@Override
	public String toString() {
		return "EmailSender [account=" + account + ", sender=" + sender + ", smtp=" + smtp + ", port=" + port
				+ ", useSSL=" + useSSL + ", body=" + body + ", subject=" + subject + ", addressTo=" + addressTo
				+ ", addressCC=" + addressCC + ", addressBCC=" + addressBCC + "]";
	}

	public void send(String sender) throws Exception {
		this.sender = sender;

		log.debug("Start send email by {}", this.toString());

		validateInputParameters();
		Properties prop = System.getProperties();
		prop.put("mail.smtp.host", smtp);
		prop.put("mail.smtp.port", port);
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.transport.protocol", "smtp");

		Session session = Session.getInstance(prop, new Authenticator() {
		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(account, password);
		    }
		});

		Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(this.sender));
        if (StringUtils.isNoneEmpty(addressTo))
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addressTo, false));
        if (StringUtils.isNoneEmpty(addressCC))
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(addressCC, false));
        if (StringUtils.isNoneEmpty(addressBCC))
            msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(addressBCC, false));
        if (StringUtils.isNoneEmpty(subject))
            msg.setSubject(subject);
        msg.setSentDate(new Date());
        if (StringUtils.isNoneEmpty(body))
            msg.setDataHandler(new DataHandler(new HTMLDataSource(body)));
        else
            msg.setDataHandler(new DataHandler(new HTMLDataSource("<html><body> </body></html>")));

        Transport.send(msg);
        log.debug("Send email finished compeletelly");
	}

	private void validateInputParameters() {
		if (StringUtils.isEmpty(smtp)) {
			throw new InvalidParameterException("SmtpAddress");
		}
		if (StringUtils.isEmpty(sender)) {
			throw new InvalidParameterException("Sender");
		}
		if (StringUtils.isEmpty(addressTo) && StringUtils.isEmpty(addressCC) && StringUtils.isEmpty(addressBCC)) {
			throw new InvalidParameterException("AddressReceipient");
		}
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setAddressTo(String addressTo) {
		this.addressTo = addressTo;
	}

	public String getAddressTo() {
		return addressTo;
	}

	public void setAddressTo(List<String> addressTo) {
		this.addressTo = getAddressesAsString(addressTo, DefaultSeparator);
	}

	public void setAddressCC(String addressCC) {
		this.addressCC = addressCC;
	}

	public void setAddressCC(List<String> addressCC) {
		this.addressCC = getAddressesAsString(addressCC, DefaultSeparator);
	}

	public void setAddressBCC(String addressBCC) {
		this.addressBCC = addressBCC;
	}

	public void setAddressBCC(List<String> addressBCC) {
		this.addressBCC = getAddressesAsString(addressBCC, DefaultSeparator);
	}

	private static class HTMLDataSource implements DataSource {

		private String html;

		public HTMLDataSource(String htmlString) {
			html = htmlString;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (html == null)
				throw new IOException("html message is null!");
			return new ByteArrayInputStream(html.getBytes());
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write HTML");
		}

		@Override
		public String getContentType() {
			return "text/html";
		}

		@Override
		public String getName() {
			return "HTMLDataSource";
		}
	}

	public String getRecipientsString() {
		return addressTo + (StringUtils.isNotBlank(addressCC)? "," + addressCC: "") + (StringUtils.isNotBlank(addressCC)? "," + addressBCC: "");
	}
}
