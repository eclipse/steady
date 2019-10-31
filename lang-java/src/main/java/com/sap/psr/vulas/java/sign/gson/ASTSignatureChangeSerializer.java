package com.sap.psr.vulas.java.sign.gson;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import java.io.IOException;

/** ASTSignatureChangeSerializer class. */
public class ASTSignatureChangeSerializer extends StdSerializer<ASTSignatureChange> {

  /** Constructor for ASTSignatureChangeSerializer. */
  public ASTSignatureChangeSerializer() {
    this(null);
  }

  /**
   * Constructor for ASTSignatureChangeSerializer.
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
  private void writeSourceCodeEntityElement(
      JsonGenerator jgen, String _property_name, SourceCodeEntity _entity) throws IOException {
    jgen.writeObjectFieldStart(_property_name);
    jgen.writeStringField("UniqueName", _entity.getUniqueName().toString());
    jgen.writeStringField("EntityType", _entity.getType().toString());
    jgen.writeStringField("Modifiers", new Integer(_entity.getModifiers()).toString());
    jgen.writeObjectFieldStart("SourceCodeRange");
    jgen.writeStringField("Start", new Integer(_entity.getSourceRange().getStart()).toString());
    jgen.writeStringField("End", new Integer(_entity.getSourceRange().getEnd()).toString());
    jgen.writeEndObject();
    jgen.writeEndObject();
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(ASTSignatureChange value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

    jgen.writeStartObject();

    jgen.writeObjectFieldStart("StructureEntity");

    jgen.writeStringField("UniqueName", value.getStructureEntity().getUniqueName());
    jgen.writeStringField("EntityType", value.getStructureEntity().getType().toString());
    jgen.writeStringField(
        "Modifiers", new Integer(value.getStructureEntity().getModifiers()).toString());

    jgen.writeArrayFieldStart("changes");

    for (SourceCodeChange change : value.getListOfChanges()) {

      jgen.writeStartObject();

      jgen.writeStringField("OperationType", change.getClass().getSimpleName());
      jgen.writeStringField("changeType", change.getChangeType().toString());

      // INSERT OPERATION
      if (change instanceof Insert) {
        this.writeSourceCodeEntityElement(jgen, "InsertedEntity", change.getChangedEntity());
        this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
      }
      // DELETE OPERATION
      else if (change instanceof Delete) {
        this.writeSourceCodeEntityElement(jgen, "DeletedEntity", change.getChangedEntity());
        this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
      }
      // MOVE OPERATION
      else if (change instanceof Move) {
        this.writeSourceCodeEntityElement(jgen, "OldParentEntity", change.getParentEntity());
        this.writeSourceCodeEntityElement(jgen, "MovedEntity", change.getChangedEntity());
        this.writeSourceCodeEntityElement(
            jgen, "NewParentEntity", ((Move) change).getNewParentEntity());
        this.writeSourceCodeEntityElement(jgen, "NewEntity", ((Move) change).getNewEntity());
      }
      // UPDATE OPERATION
      else if (change instanceof Update) {
        this.writeSourceCodeEntityElement(jgen, "NewEntity", ((Update) change).getNewEntity());
        this.writeSourceCodeEntityElement(
            jgen, "UpdatedEntity", ((Update) change).getChangedEntity());
        this.writeSourceCodeEntityElement(jgen, "ParentEntity", change.getParentEntity());
      }

      jgen.writeEndObject();
    }
    jgen.writeEndArray(); // changes
    jgen.writeEndObject(); // StructureEntity
    jgen.writeEndObject();
  }
}
