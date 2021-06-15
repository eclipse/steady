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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java.sign.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.eclipse.steady.java.sign.ASTConstructBodySignature;
import org.eclipse.steady.java.sign.ASTUtil;
import org.eclipse.steady.sign.Signature;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 *  This class is used to deserialize an AST JSON representation of a construct body object into its corresponding
 *  java Signatures object
 * 
 *JSON representation of ASTConstructBodySignature:
 *
 *Node:
 *    - fLabel : EntityType
 *    - fValue : String
 *
 *    -- fMatched : boolean
 *    -- fOrdered : boolean
 *
 *    - fEntity : SourceCodeEntity
 *		 -  fUniqueName : String  //same information as fValue above
 *		 -  fType : EntityType    //same information ast fLabel above
 *		 -  fModifiers : int	 // this is always zero (Not sure if i should have it in the json object)
 *		 -  fAssociatedEntities : List&lt;SourceCodeEntity&gt;
 *		 -  SourceRange fRange : SourceRange
 *				- end : int
 *				- start : int
 *    -  fAssociatedNodes : List&lt;Node&gt;
 */
public class ASTSignatureDeserializer implements JsonDeserializer<Signature> {

  // Values for JsonElement Names, used as a member name during deserialization
  // We need only to change these value if the name/value pair of a JsonElement changes in the json
  // representation of the signature
  // For instance, if we want to  compress  the json object for not storing a large data in the DB.
  private static String AST = "ast";
  private static String ENTITY_TYPE = "EntityType";
  private static String VALUE = "Value";
  private static String CHILDREN = "C";

  private static String SOURCE_CODE_ENTITY = "SourceCodeEntity";
  private static String SRC_ENTITY_MODIFIERS = "Modifiers";
  private static String SRC_ENTITY_SOURCE_RANGE = "SourceRange";
  private static String SRC_ENTITY_SOURCE_RANGE_END = "End";
  private static String SRC_ENTITY_SOURCE_RANGE_START = "Start";

  // ASTConstructBodySignature
  // private ASTConstructBodySignature astConstructSign = null;

  // Signature
  // private Signature sign = null;

  /** {@inheritDoc} */
  @Override
  public Signature deserialize(
      final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {

    // The Deserialization logic goes here
    final JsonObject jsonObject = json.getAsJsonObject();
    final JsonObject astRootJsonObject =
        jsonObject.get(AST).getAsJsonArray().get(0).getAsJsonObject();

    String fValue = astRootJsonObject.get(VALUE).getAsString(); // Retrieve the Value
    String strLabel = astRootJsonObject.get(ENTITY_TYPE).getAsString();
    EntityType fLabel = ASTUtil.getJavaEntityType(strLabel); // Retrieve the EntityType

    // Instantiate the ASTConstructBodySignature object here (We can have instance of Node object
    // with the above two informations)
    ASTConstructBodySignature astConstructSign = new ASTConstructBodySignature(fLabel, fValue);

    // Create the Root Node
    astConstructSign.setRoot(astConstructSign);

    // TODO : Check if these setting are really necessary to compute the "diff" as an input to
    // changedistiller
    // Settings for order and matching
    // astConstructSign.enableInOrder();
    // astConstructSign.enableMatched();

    // Retrieve the Children
    JsonElement jsonChildrenElement = astRootJsonObject.get(CHILDREN);
    if (jsonChildrenElement != null) {

      JsonArray jsonChildrenArray = astRootJsonObject.get(CHILDREN).getAsJsonArray();
      // Add the children of a Node
      final Node[] children = new Node[jsonChildrenArray.size()];

      for (int i = 0; i < children.length; i++) {
        final JsonObject jsonChildren = jsonChildrenArray.get(i).getAsJsonObject();

        // Get the child Node element and add it
        Node newChild = getNodeChildElement(jsonChildren);
        astConstructSign.add(newChild);
      }
    }
    // Retrieve the SourceCodeEntity
    SourceCodeEntity srcCodeEntity = null;
    JsonObject srcCodeEntityJsonObject =
        astRootJsonObject.get(SOURCE_CODE_ENTITY).getAsJsonObject();
    int modifiers = srcCodeEntityJsonObject.get(SRC_ENTITY_MODIFIERS).getAsInt();

    // SourceRange JsonObject
    JsonObject srcCodeEntitySrcRangeJsonObject =
        srcCodeEntityJsonObject.get(SRC_ENTITY_SOURCE_RANGE).getAsJsonObject();
    int srcRangeStart =
        srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_START).getAsInt();
    int srcRangeEnd = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_END).getAsInt();
    SourceRange srcRange = new SourceRange(srcRangeStart, srcRangeEnd);
    srcCodeEntity = new SourceCodeEntity(fValue, fLabel, modifiers, srcRange);
    astConstructSign.setEntity(srcCodeEntity);

    return astConstructSign;
  }

  private Node getNodeChildElement(JsonObject jsonObject) {

    String fValue = jsonObject.get(VALUE).getAsString(); // Retrieve the Value
    String strLabel = jsonObject.get(ENTITY_TYPE).getAsString();
    EntityType fLabel = ASTUtil.getJavaEntityType(strLabel); // Retrieve the EntityType

    // Instantiate the Node object here
    Node childNode = new Node(fLabel, fValue);

    // Retrieve the SourceCodeEntity
    SourceCodeEntity srcCodeEntity = null;
    JsonObject srcCodeEntityJsonObject = jsonObject.get(SOURCE_CODE_ENTITY).getAsJsonObject();
    int modifiers = srcCodeEntityJsonObject.get(SRC_ENTITY_MODIFIERS).getAsInt();

    // SourceRange JsonObject
    JsonObject srcCodeEntitySrcRangeJsonObject =
        srcCodeEntityJsonObject.get(SRC_ENTITY_SOURCE_RANGE).getAsJsonObject();
    int srcRangeStart =
        srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_START).getAsInt();
    int srcRangeEnd = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_END).getAsInt();
    SourceRange srcRange = new SourceRange(srcRangeStart, srcRangeEnd);
    srcCodeEntity = new SourceCodeEntity(fValue, fLabel, modifiers, srcRange);

    JsonElement jsonChildrenElement = jsonObject.get(CHILDREN);
    if (jsonChildrenElement != null) {

      JsonArray jsonChildrenArray = jsonChildrenElement.getAsJsonArray();

      // Add the children of a Node
      final Node[] children = new Node[jsonChildrenArray.size()];

      for (int i = 0; i < children.length; i++) {
        final JsonObject jsonChildren = jsonChildrenArray.get(i).getAsJsonObject();

        // Get the child Node element and add it
        Node newChild = getNodeChildElement(jsonChildren);
        childNode.add(newChild);
      }
    }

    childNode.setEntity(srcCodeEntity);
    return childNode;
  }
}
