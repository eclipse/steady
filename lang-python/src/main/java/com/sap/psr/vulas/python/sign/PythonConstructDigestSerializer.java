package com.sap.psr.vulas.python.sign;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/** PythonConstructDigestSerializer class. */
public class PythonConstructDigestSerializer extends StdSerializer<PythonConstructDigest> {

  /** Constructor for PythonConstructDigestSerializer. */
  public PythonConstructDigestSerializer() {
    this(null);
  }

  /**
   * Constructor for PythonConstructDigestSerializer.
   *
   * @param t a {@link java.lang.Class} object.
   */
  public PythonConstructDigestSerializer(Class<PythonConstructDigest> t) {
    super(t);
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(
      PythonConstructDigest value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeStringField("computedFrom", value.getComputedFrom());
    jgen.writeStringField("computedFromType", value.getComputedFromType().toString());
    jgen.writeStringField("digest", value.getDigest());
    jgen.writeStringField("digestAlgorihtm", value.getDigestAlgorithm().toString());
    jgen.writeEndObject();
  }
}
