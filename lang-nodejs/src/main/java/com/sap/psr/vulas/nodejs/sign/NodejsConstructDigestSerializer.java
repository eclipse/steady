package com.sap.psr.vulas.nodejs.sign;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class NodejsConstructDigestSerializer extends StdSerializer<NodejsConstructDigest> {

    public NodejsConstructDigestSerializer() {
        this(null);
    }

    public NodejsConstructDigestSerializer(Class<NodejsConstructDigest> t) {
        super(t);
    }

    @Override
    public void serialize(NodejsConstructDigest value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("computedFrom", value.getComputedFrom());
        jgen.writeStringField("computedFromType", value.getComputedFromType().toString());
        jgen.writeStringField("digest", value.getDigest());
        jgen.writeStringField("digestAlgorihtm", value.getDigestAlgorithm().toString());
        jgen.writeEndObject();
    }
}
