package com.sap.psr.vulas.backend.util;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Encapsulates the most basic elements of an email message. */
public class Message {

  private String subject = null;

  private String body = null;

  private String sender = null;

  private Set<String> recipients = new HashSet<String>();

  private Path attachment = null;

  /**
   * Getter for the field <code>subject</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Setter for the field <code>subject</code>.
   *
   * @param subject a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Getter for the field <code>body</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBody() {
    return body;
  }

  /**
   * Setter for the field <code>body</code>.
   *
   * @param body a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message setBody(String body) {
    this.body = body;
    return this;
  }

  /**
   * Getter for the field <code>sender</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSender() {
    return sender;
  }

  /**
   * Setter for the field <code>sender</code>.
   *
   * @param sender a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message setSender(String sender) {
    this.sender = sender;
    return this;
  }

  /**
   * addRecipient.
   *
   * @param _r a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message addRecipient(String _r) {
    this.recipients.add(_r);
    return this;
  }

  /**
   * Getter for the field <code>recipients</code>.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<String> getRecipients() {
    return recipients;
  }

  /**
   * Setter for the field <code>recipients</code>.
   *
   * @param recipients a {@link java.util.Set} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message setRecipients(Set<String> recipients) {
    this.recipients = recipients;
    return this;
  }

  /**
   * Getter for the field <code>attachment</code>.
   *
   * @return a {@link java.nio.file.Path} object.
   */
  public Path getAttachment() {
    return attachment;
  }

  /**
   * Setter for the field <code>attachment</code>.
   *
   * @param attachment a {@link java.nio.file.Path} object.
   * @return a {@link com.sap.psr.vulas.backend.util.Message} object.
   */
  public Message setAttachment(Path attachment) {
    this.attachment = attachment;
    return this;
  }

  /**
   * hasAttachment.
   *
   * @return a boolean.
   */
  public boolean hasAttachment() {
    return this.attachment != null;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("[from=")
        .append(this.getSender())
        .append(", to=")
        .append(this.getRecipients())
        .append(", subject=")
        .append(this.getSubject());
    if (this.attachment != null)
      b.append(", attachment=").append(this.attachment.getFileName().toString());
    b.append("]");
    return b.toString();
  }
}
