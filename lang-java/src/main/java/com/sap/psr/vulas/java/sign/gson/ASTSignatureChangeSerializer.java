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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.shared.json.JsonBuilder;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;

/**
 * <p>ASTSignatureChangeSerializer class.</p>
 *
 */
public class ASTSignatureChangeSerializer extends StdSerializer<ASTSignatureChange> {

	/**
	 * <p>Constructor for ASTSignatureChangeSerializer.</p>
	 */
	public ASTSignatureChangeSerializer() {
		this(null);
	}

	/**
	 * <p>Constructor for ASTSignatureChangeSerializer.</p>
	 *
	 * @param t a {@link java.lang.Class} object.
	 */
	public ASTSignatureChangeSerializer(Class<ASTSignatureChange> t) {
		super(t);
	}
	
	/**
	 * Helper method for building a "SourceCodeEntity" element
	 *
	 * @param change - SourcCodeChangeElement
	 * @param buffer
	 */
	private void writeSourceCodeEntityElement(JsonGenerator jgen, String _property_name, SourceCodeEntity _entity) throws IOException {
		jgen.writeObjectFieldStart(_property_name);
		jgen.writeStringField("UniqueName", _entity.getUniqueName().toString());
		jgen.writeStringField("EntityType", _entity.getType().toString());
		jgen.writeStringField("Modifiers", Integer.toString(_entity.getModifiers()));
		jgen.writeObjectFieldStart("SourceCodeRange");
		jgen.writeStringField("Start", Integer.toString(_entity.getSourceRange().getStart()));
		jgen.writeStringField("End", Integer.toString(_entity.getSourceRange().getEnd()));
		jgen.writeEndObject();
		jgen.writeEndObject();
	}

	/** {@inheritDoc} */
	@Override
	public void serialize(ASTSignatureChange value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

		jgen.writeStartObject();

		jgen.writeObjectFieldStart("StructureEntity");

		jgen.writeStringField("UniqueName", value.getStructureEntity().getUniqueName());
		jgen.writeStringField("EntityType", value.getStructureEntity().getType().toString());
		jgen.writeStringField("Modifiers", Integer.toString(value.getStructureEntity().getModifiers()));

		jgen.writeArrayFieldStart("changes");

		for(SourceCodeChange change : value.getListOfChanges()) {
			
			jgen.writeStartObject();
			
			jgen.writeStringField("OperationType", change.getClass().getSimpleName());
			jgen.writeStringField("changeType", change.getChangeType().toString());
						
			// INSERT OPERATION
			if(change instanceof Insert) {
				this.writeSourceCodeEntityElement(jgen, "InsertedEntity", change.getChangedEntity());
				this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
			}
			//DELETE OPERATION
			else if(change instanceof Delete) {
				this.writeSourceCodeEntityElement(jgen, "DeletedEntity", change.getChangedEntity());
				this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
			}
			//MOVE OPERATION
			else if(change instanceof Move) {
				this.writeSourceCodeEntityElement(jgen, "OldParentEntity", change.getParentEntity());
				this.writeSourceCodeEntityElement(jgen, "MovedEntity", change.getChangedEntity());
				this.writeSourceCodeEntityElement(jgen, "NewParentEntity", ((Move)change).getNewParentEntity());
				this.writeSourceCodeEntityElement(jgen, "NewEntity", ((Move)change).getNewEntity());
			}
			//UPDATE OPERATION
			else if(change instanceof Update) {
				this.writeSourceCodeEntityElement(jgen, "NewEntity", ((Update)change).getNewEntity());
				this.writeSourceCodeEntityElement(jgen, "UpdatedEntity", ((Update)change).getChangedEntity());
				this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
			}
			
			jgen.writeEndObject();
		}
		jgen.writeEndArray(); // changes
		jgen.writeEndObject(); // StructureEntity
		jgen.writeEndObject();
	}
}
