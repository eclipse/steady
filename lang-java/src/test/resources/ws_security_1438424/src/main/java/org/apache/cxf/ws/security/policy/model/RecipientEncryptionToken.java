/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cxf.ws.security.policy.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.cxf.ws.security.policy.SPConstants;

public class RecipientEncryptionToken extends TokenWrapper {

  public RecipientEncryptionToken(SPConstants version, PolicyBuilder b) {
    super(version, b);
  }

  /** @return Returns the receipientEncryptionToken. */
  public Token getRecipientEncryptionToken() {
    return getToken();
  }

  /** @param receipientEncryptionToken The receipientEncryptionToken to set. */
  public void setRecipientEncryptionToken(Token recipientEncryptionToken) {
    setToken(recipientEncryptionToken);
  }

  public QName getRealName() {
    return constants.getRecipientEncryptionToken();
  }

  public QName getName() {
    return SP12Constants.INSTANCE.getRecipientEncryptionToken();
  }

  public void serialize(XMLStreamWriter writer) throws XMLStreamException {
    String localName = getRealName().getLocalPart();
    String namespaceURI = getRealName().getNamespaceURI();

    String prefix = writer.getPrefix(namespaceURI);

    if (prefix == null) {
      prefix = getRealName().getPrefix();
      writer.setPrefix(prefix, namespaceURI);
    }

    // <sp:RecipientEncryptionToken>
    writer.writeStartElement(prefix, localName, namespaceURI);

    String pPrefix = writer.getPrefix(SPConstants.POLICY.getNamespaceURI());
    if (pPrefix == null) {
      pPrefix = SPConstants.POLICY.getPrefix();
      writer.setPrefix(pPrefix, SPConstants.POLICY.getNamespaceURI());
    }

    // <wsp:Policy>
    writer.writeStartElement(
        pPrefix, SPConstants.POLICY.getLocalPart(), SPConstants.POLICY.getNamespaceURI());

    Token token = getRecipientEncryptionToken();
    if (token == null) {
      throw new RuntimeException("RecipientEncryptionToken doesn't contain any token assertions");
    }
    token.serialize(writer);

    // </wsp:Policy>
    writer.writeEndElement();

    // </sp:RecipientEncryptionToken>
    writer.writeEndElement();
  }
}
