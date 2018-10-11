package com.sap.psr.vulas.backend.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

public class SmtpClientTest {

	//@Test
	public void testSend() throws MessagingException {
		final Message msg = new Message();
		msg.setSender("henrik.plate@sap.com");
		msg.setSubject("Test");
		msg.setBody("Test");
		msg.addRecipient("henrik.plate@sap.com");
		
		final SmtpClient client = new SmtpClient();
		client.send(msg);
	}
	
	@Test
	public void testTransform() {
		final Set<String> addresses = new HashSet<String>();
		assertEquals(0, SmtpClient.transform(addresses).length);
		addresses.add("foo@bar.com");
		assertEquals(1, SmtpClient.transform(addresses).length);		
	}
}
