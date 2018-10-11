package com.sap.psr.vulas.backend.util;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class SmtpClient {
	
	private static Logger log = LoggerFactory.getLogger(SmtpClient.class);
	
	public final static String SMTP_HOST = "vulas.backend.smtp.host";
	public final static String SMTP_PORT = "vulas.backend.smtp.port";
	public final static String SMTP_USER = "vulas.backend.smtp.user";
	public final static String SMTP_PWD  = "vulas.backend.smtp.pwd";
	
	private Properties props = null;
	
	public SmtpClient() {
		props = new Properties();
		props.put("mail.smtp.auth", "true");
		//props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_HOST));
		props.put("mail.smtp.port", VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_PORT));
	}
		
	public void send(@NotNull com.sap.psr.vulas.backend.util.Message _msg) throws MessagingException {
		if(_msg.hasAttachment())
			this.sendWithAttachement(_msg);
		else
			log.error("Messages without attachment are not supported");
	}
	
	private void sendWithAttachement(@NotNull com.sap.psr.vulas.backend.util.Message _msg) throws MessagingException {
		final StopWatch sw = new StopWatch("Send message " + _msg).start();

		final Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_USER), VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_PWD));
			}
		});

		try {
			final Message message = new MimeMessage(session);
			message.setSubject(_msg.getSubject());
			message.setFrom(new InternetAddress(_msg.getSender()));
			
			// Recipients
			final InternetAddress[] to = SmtpClient.transform(_msg.getRecipients());
			if(to.length==0)
				throw new MessagingException("No valid recipients in " + _msg.getRecipients());
			else 
				message.setRecipients(Message.RecipientType.TO,	to);
			
			// Create a multipart message
			Multipart multipart = new MimeMultipart();

			// Text
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(_msg.getBody());
			multipart.addBodyPart(messageBodyPart);

			// Attachment
			messageBodyPart = new MimeBodyPart();
			final DataSource source = new FileDataSource(_msg.getAttachment().toFile());
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(_msg.getAttachment().getFileName().toString());
			multipart.addBodyPart(messageBodyPart);

			// Send
			message.setContent(multipart);
			Transport.send(message);
			sw.stop();
		} catch (MessagingException e) {
			sw.stop(e);
			throw e;
		}
	}
	
	static final InternetAddress[] transform(Set<String> _recipients) {
		Set<InternetAddress> recipients = new HashSet<InternetAddress>();
		for(String to : _recipients) {
			try {
				recipients.add(InternetAddress.parse(to)[0]);
			} catch (AddressException e) {
				log.error("[" + to + "] is not a valid email address");
			}
		}
		return recipients.toArray(new InternetAddress[recipients.size()]);
	}
}
