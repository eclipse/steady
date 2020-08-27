/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.backend.util;

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

import org.apache.commons.configuration.Configuration;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SmtpClient class.</p>
 *
 */
public class SmtpClient {

  private static Logger log = LoggerFactory.getLogger(SmtpClient.class);

  /** Constant <code>SMTP_HOST="vulas.backend.smtp.host"</code> */
  public static final String SMTP_HOST = "vulas.backend.smtp.host";
  /** Constant <code>SMTP_PORT="vulas.backend.smtp.port"</code> */
  public static final String SMTP_PORT = "vulas.backend.smtp.port";
  /** Constant <code>SMTP_USER="vulas.backend.smtp.user"</code> */
  public static final String SMTP_USER = "vulas.backend.smtp.user";
  /** Constant <code>SMTP_PWD="vulas.backend.smtp.pwd"</code> */
  public static final String SMTP_PWD = "vulas.backend.smtp.pwd";

  private Properties props = null;

  /**
   * <p>Constructor for SmtpClient.</p>
   *
   * @throws java.lang.IllegalStateException if any.
   */
  public SmtpClient() throws IllegalStateException {
    this.props = SmtpClient.getSmtpProperties(VulasConfiguration.getGlobal().getConfiguration());
  }

  /**
   * <p>send.</p>
   *
   * @param _msg a {@link org.eclipse.steady.backend.util.Message} object.
   * @throws javax.mail.MessagingException if any.
   */
  public void send(@NotNull org.eclipse.steady.backend.util.Message _msg) throws MessagingException {
    if (_msg.hasAttachment()) this.sendWithAttachement(_msg);
    else log.error("Messages without attachment are not supported");
  }

  private void sendWithAttachement(@NotNull org.eclipse.steady.backend.util.Message _msg)
      throws MessagingException {
    final StopWatch sw = new StopWatch("Send message " + _msg).start();

    final Session session =
        Session.getInstance(
            props,
            new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_USER),
                    VulasConfiguration.getGlobal().getConfiguration().getString(SMTP_PWD));
              }
            });

    try {
      final Message message = new MimeMessage(session);
      message.setSubject(_msg.getSubject());
      message.setFrom(new InternetAddress(_msg.getSender()));

      // Recipients
      final InternetAddress[] to = SmtpClient.transform(_msg.getRecipients());
      if (to.length == 0)
        throw new MessagingException("No valid recipients in " + _msg.getRecipients());
      else message.setRecipients(Message.RecipientType.TO, to);

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
    for (String to : _recipients) {
      try {
        recipients.add(InternetAddress.parse(to)[0]);
      } catch (AddressException e) {
        log.error("[" + to + "] is not a valid email address");
      }
    }
    return recipients.toArray(new InternetAddress[recipients.size()]);
  }

  /**
   * Returns {@link Properties} with all SMTP settings. Throws an {@link IllegalStateException} if
   * {@link #SMTP_HOST} or {@link SMTP_PORT} are not set.
   *
   * @param _cfg a {@link org.apache.commons.configuration.Configuration} object.
   * @return a {@link java.util.Properties} object.
   * @throws java.lang.IllegalStateException if any.
   */
  public static Properties getSmtpProperties(Configuration _cfg) throws IllegalStateException {
    final String host = _cfg.getString(SMTP_HOST);
    final String port = _cfg.getString(SMTP_PORT);
    if (host == null || port == null)
      throw new IllegalStateException(
          "Cannot setup SMTP client, configuration settings ["
              + SMTP_HOST
              + "] and/or ["
              + SMTP_PORT
              + "] are not present");

    final Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    // props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", port);

    return props;
  }
}
