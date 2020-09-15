package org.eclipse.steady.python.sign;

import java.io.IOException;

import org.eclipse.steady.python.sign.PythonConstructDigest.ComputedFromType;
import org.eclipse.steady.shared.enums.DigestAlgorithm;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/** PythonConstructDigestDeserializer class. */
public class PythonConstructDigestDeserializer extends StdDeserializer<PythonConstructDigest> {

  /** Constructor for PythonConstructDigestDeserializer. */
  public PythonConstructDigestDeserializer() {
    this(null);
  }

  /**
   * Constructor for PythonConstructDigestDeserializer.
   *
   * @param t a {@link java.lang.Class} object.
   */
  public PythonConstructDigestDeserializer(Class<PythonConstructDigest> t) {
    super(t);
  }

  /** {@inheritDoc} */
  @Override
  public PythonConstructDigest deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    final JsonNode json_root = p.getCodec().readTree(p);
    String computedFrom = json_root.findValue("computedFrom").asText();
    ComputedFromType computedFromType =
        ComputedFromType.valueOf(json_root.findValue("computedFromType").asText());
    String digest = json_root.findValue("digest").asText();
    DigestAlgorithm digestAlgorithm =
        DigestAlgorithm.fromString(json_root.findValue("digestAlgorithm").asText());

    PythonConstructDigest pythonConstructDigest =
        new PythonConstructDigest(digest, digestAlgorithm);
    pythonConstructDigest.setComputedFrom(computedFrom);
    pythonConstructDigest.setComputedFromType(computedFromType);
    return pythonConstructDigest;
  }
}
