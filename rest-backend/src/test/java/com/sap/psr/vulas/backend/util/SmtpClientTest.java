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
package com.sap.psr.vulas.backend.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;

import org.junit.Test;

public class SmtpClientTest {

    // @Test
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
