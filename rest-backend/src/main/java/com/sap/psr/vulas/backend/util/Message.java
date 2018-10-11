package com.sap.psr.vulas.backend.util;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates the most basic elements of an email message.
 *
 */
public class Message {

	private String subject = null;
	
	private String body = null;
	
	private String sender = null;
	
	private Set<String> recipients = new HashSet<String>();

	private Path attachment = null;

	public String getSubject() {
		return subject;
	}

	public Message setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public String getBody() {
		return body;
	}

	public Message setBody(String body) {
		this.body = body;
		return this;
	}

	public String getSender() {
		return sender;
	}

	public Message setSender(String sender) {
		this.sender = sender;
		return this;
	}

	public Message addRecipient(String _r) {
		this.recipients.add(_r);
		return this;
	}
	
	public Set<String> getRecipients() {
		return recipients;
	}

	public Message setRecipients(Set<String> recipients) {
		this.recipients = recipients;
		return this;
	}

	public Path getAttachment() {
		return attachment;
	}

	public Message setAttachment(Path attachment) {
		this.attachment = attachment;
		return this;
	}
	
	public boolean hasAttachment() {
		return this.attachment!=null;
	}
	
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[from=").append(this.getSender()).append(", to=").append(this.getRecipients()).append(", subject=").append(this.getSubject());
		if(this.attachment!=null)
			b.append(", attachment=").append(this.attachment.getFileName().toString());
		b.append("]");
		return b.toString();
	}
}
