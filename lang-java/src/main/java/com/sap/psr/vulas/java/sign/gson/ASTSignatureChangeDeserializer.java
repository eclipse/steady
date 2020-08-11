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
package com.sap.psr.vulas.java.sign.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.ASTUtil;
import com.sap.psr.vulas.sign.SignatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;

/**
 *  This class is used to deserialize an AST JSON representation of a signature change object into its corresponding
 *  java object
 *   Note : One needs to take a look at the source code of changedistiller to understand the elements that are employed for the Deserialization
 */

/**
 * 
 * /**
 *
 * JSON representation of ASTSignatureChange:
 *
 * public class ASTSignatureChange extends DistillerUtil implements
 * SignatureChange
 *
 * public class ASTConstructBodySignature extends ASTSignature public abstract
 * class ASTSignature extends Node implements Signature
 *
 *
 * SourceCodeChange
 *
 * 1. private ChangeType fChangeType = ChangeType.UNCLASSIFIED_CHANGE;
 *
 * //Structure entity in which the change operation happened, e.g., attribute,
 * class, or method. 2. private StructureEntityVersion fRootEntity;
 *
 * 3. private SourceCodeEntity fChangedEntity; - fUniqueName : String //same
 * information as fValue above - fType : EntityType //same information ast
 * fLabel above - fModifiers : int // this is always zero (Not sure if i should
 * have it in the json object) - fAssociatedEntities :
 * List&lt;SourceCodeEntity&gt; - SourceRange fRange : SourceRange - end : int -
 * start : int
 *
 * //Source code entity that becomes the parent entity when the change is
 * applied. 4. private SourceCodeEntity fParentEntity;
 *
 *
 *
 *
 * public class ASTSignatureChange{
 *
 * private ChangeType changeType; private StructureEntityVersion
 * StructureEntityVersion; private SourceCodeEntity changedEntity; private
 * SourceCodeEntity r }
 *
 *
 * A Source code change has this JSON object format { "changeType" : " ",
 * "StructureEntityVersion" : { "EntityType" : "", "UniqueName" : "",
 * "Modifiers" : "", // might be discarded, int "Version " : "" },
 * "changedEntity" : { "UniqueName" : "", "EntityType " : "", "Modifiers" : "",
 * // might be discarded, int "SourceRange" : { "start" : "", "end" : "" } } ,
 * "parentEntity" : { "UniqueName" : "", "EntityType " : "", "Modifiers" : "",
 * // might be discarded, int "SourceRange" : { "start" : "", "end" : "" }
 *
 * } }
 */
public class ASTSignatureChangeDeserializer extends StdDeserializer<SignatureChange> {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

	// Values for JsonElement Names, used as a member name during deserialization
	// We need only to change these value if the name/value pair of a JsonElement
	// changes in the json representation of the ASTSignatureChange
	// For instance, if we want to compress the json object - not to store a large
	// amount data in the DB.
	private static String SIGN_CHANGE = "vulasChange";
	private static String CHANGE_TYPE = "changeType";

	// StructureEntity, ROOT Entity
	private static String STRUCTURE_ENTITY = "StructureEntity";
	private static String STRUCTURE_ENTITY_UNIQUE_NAME = "UniqueName";
	private static String STRUCTURE_ENTITY_ENTITY_TYPE = "EntityType";
	private static String STRUCTURE_ENTITY_MODIFIERS = "Modifiers";
	private static String STRUCTURE_ENTITY_CHANGES = "changes";

	// ChagedEntity
	private static String SOURCE_CODE_ENTITY = "SourceCodeEntity";
	private static String SRC_ENTITY_UNIQUE_NAME = "UniqueName";
	private static String SRC_ENTITY_TYPE = "EntityType";
	private static String SRC_ENTITY_MODIFIERS = "Modifiers";
	private static String SRC_ENTITY_SOURCE_RANGE = "SourceCodeRange";
	private static String SRC_ENTITY_SOURCE_RANGE_END = "End";
	private static String SRC_ENTITY_SOURCE_RANGE_START = "Start";

	// parentEntity
	private static String PARENT_SOURCE_CODE_ENTITY = "parentEntity";
	private static String PARENT_SRC_ENTITY_UNIQUE_NAME = "UniqueName";
	private static String PARENT_SRC_ENTITY_TYPE = "EntityType";
	private static String PARENT_SRC_ENTITY_MODIFIERS = "Modifiers";
	private static String PARENT_SRC_ENTITY_SOURCE_RANGE = "SourceRange";
	private static String PARENT_SRC_ENTITY_SOURCE_RANGE_END = "End";
	private static String PARENT_SRC_ENTITY_SOURCE_RANGE_START = "Start";

	// private Set<SourceCodeChange> srcCodeChanges;
	// private SourceCodeChange srcCodeChange;
	// private StructureEntityVersion rootEntity = null;
	// List of SourceCodeChange modifications
	// private List<SourceCodeChange> srcCodeChangeList= new
	// ArrayList<SourceCodeChange>();

	// ASTSignatureChange
	// private ASTSignatureChange astSignChange = null;

	// Signature
	// private SignatureChange signChange = null;
	// private ChangeType changeType = null;

	/**
	 * <p>
	 * Constructor for ASTSignatureChangeDeserializer.
	 * </p>
	 */
	public ASTSignatureChangeDeserializer() {
		this(null);
	}

	/**
	 * <p>
	 * Constructor for ASTSignatureChangeDeserializer.
	 * </p>
	 *
	 * @param t a {@link java.lang.Class} object.
	 */
	public ASTSignatureChangeDeserializer(Class<SignatureChange> t) {
		super(t);
	}

	
	/**
	 * Helper method for retrieving a SourceCodeEntity element
	 */
	private SourceCodeEntity getSourceCodeEntityElement(JsonNode srcCodeEntityJsonObject) {

		// Retrieve the SourceCodeEntity
		SourceCodeEntity srcCodeEntity = null;
		String fValue = srcCodeEntityJsonObject.get("UniqueName").asText(); // Retrieve the Value
		String strLabel = srcCodeEntityJsonObject.get("EntityType").asText();
		EntityType fLabel = ASTUtil.getJavaEntityType(strLabel); // Retrieve the EntityType
		int modifiers = srcCodeEntityJsonObject.get(SRC_ENTITY_MODIFIERS).asInt();

		// SourceRange JsonObject
		JsonNode srcCodeEntitySrcRangeJsonObject = srcCodeEntityJsonObject.get(SRC_ENTITY_SOURCE_RANGE);
		int srcRangeStart = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_START).asInt();
		int srcRangeEnd = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_END).asInt();
		SourceRange srcRange = new SourceRange(srcRangeStart, srcRangeEnd);
		srcCodeEntity = new SourceCodeEntity(fValue, fLabel, modifiers, srcRange);

		srcCodeEntity = new SourceCodeEntity(fValue, fLabel, modifiers, srcRange);
		return srcCodeEntity;
	}

	@Override
	public SignatureChange deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		// HP, 10.12.2015, the following were members, now moved into method
		SourceCodeChange srcCodeChange;
		StructureEntityVersion rootEntity = null;
		List<SourceCodeChange> srcCodeChangeList = new ArrayList<SourceCodeChange>();
		ChangeType changeType = null;

		// The deserialization logic goes here
		
		final JsonNode json_root = p.getCodec().readTree(p);
		final List<JsonNode> strEntity_nodes = json_root.findValues(STRUCTURE_ENTITY);
		final JsonNode strEntity_root  = strEntity_nodes.get(0);
		String uniqueName = strEntity_root.findValue(STRUCTURE_ENTITY_UNIQUE_NAME).asText();
		EntityType type = ASTUtil.getJavaEntityType(strEntity_root.findValue(STRUCTURE_ENTITY_ENTITY_TYPE).asText());
		int modifiers = strEntity_root.findValue(SRC_ENTITY_MODIFIERS).asInt();
		
		
		rootEntity = new StructureEntityVersion(type, uniqueName, modifiers);
		
		
		JsonNode changesJsonArray = strEntity_root.get("changes");
		SourceCodeChange[] srcCodeChanges = new SourceCodeChange[changesJsonArray.size()];
		//Read in the Value the array from the "changes" element
		if (changesJsonArray.isArray()) {
		    for (final JsonNode change : changesJsonArray) {
		    	//Depending on the "OperationType" of the "changes" element, initialize the appropriate sourceCodeChange element (Insert, Update, Move, Delete)
				String operationType = change.findValue("OperationType").asText();
				/*
				 * switch(operationType) {
				 */
				// case "Insert":
				if (operationType.equals("Insert")) {

					changeType = ASTUtil.getChangeType(change.findValue("changeType").asText());
					SourceCodeEntity insertedEntity = this
							.getSourceCodeEntityElement(change.findValue("InsertedEntity"));
					SourceCodeEntity parentEntity = this
							.getSourceCodeEntityElement(change.findValue("ParentEntity"));

					srcCodeChange = new Insert(changeType, rootEntity, // StructureEntityVersion
							insertedEntity, // New Inserted Entity
							parentEntity); // ParentEntity for the New Inserted Node

					// Add it to the List<SourceCodeChanges>
					srcCodeChangeList.add(srcCodeChange);
					// break;

				}

				else if (operationType.equals("Move")) {
//		case "Move":
					changeType = ASTUtil.getChangeType(change.get("changeType").asText());
					SourceCodeEntity movedEntity = this
							.getSourceCodeEntityElement(change.get("MovedEntity"));
					SourceCodeEntity newEntity = this
							.getSourceCodeEntityElement(change.get("NewEntity"));
					SourceCodeEntity oldParentEntity = this
							.getSourceCodeEntityElement(change.get("OldParentEntity"));
					SourceCodeEntity newParentEntity = this.getSourceCodeEntityElement(
							change.get("NewParentEntity"));

					// srcCodeChange.setRootEntity(rootEntity); //StructureEntityVersion
					srcCodeChange = new Move(changeType, rootEntity, movedEntity, newEntity, oldParentEntity,
							newParentEntity);

					// Add it to the List<SourceCodeChanges>
					srcCodeChangeList.add(srcCodeChange);
					// break;
				}

				// case "Update":
				else if (operationType.equals("Update")) {
					changeType = ASTUtil.getChangeType(change.get("changeType").asText());

					SourceCodeEntity updatedEntity = this
							.getSourceCodeEntityElement(change.get("UpdatedEntity"));
					SourceCodeEntity newEntityUpdate = this
							.getSourceCodeEntityElement(change.get("NewEntity"));
					SourceCodeEntity parentEntityUpdate = this
							.getSourceCodeEntityElement(change.get("ParentEntity"));

					srcCodeChange = new Update(changeType, rootEntity, updatedEntity, // Old Updated Entity
							newEntityUpdate, // New Updated Entity
							parentEntityUpdate); // Same Parent Entity

					// Add it to the Set<SourceCodeChanges>
					srcCodeChangeList.add(srcCodeChange);

					// break;
				}

				// case "Delete":

				else if (operationType.equals("Delete")) {
					changeType = ASTUtil.getChangeType(change.get("changeType").asText());
					SourceCodeEntity deletedEntity = this
							.getSourceCodeEntityElement(change.get("DeletedEntity"));
					SourceCodeEntity parentEntityDelete = this
							.getSourceCodeEntityElement(change.get("ParentEntity"));

					srcCodeChange = new Delete(changeType, rootEntity, // StructureEntityVersion;
							deletedEntity, // Deleted Entity
							parentEntityDelete); // ParentEntity

					// Add it to the Set<SourceCodeChanges>
					srcCodeChangeList.add(srcCodeChange);

					// break;
				} else {
					log.info("Unsupported Operation Type" + operationType);
				}

				/*
				 * default: }
				 */

			} //closes for loop

		}
		
	    //Add the List<SourceCodeChange> to the StructureEntityVersion instance
	    rootEntity.addAllSourceCodeChanges(srcCodeChangeList);

	    //astSignChange = new ASTSignatureChange(srcCodeChangeList);
		ASTSignatureChange astSignChange = new ASTSignatureChange(rootEntity);
		return astSignChange;
		
	}
}
