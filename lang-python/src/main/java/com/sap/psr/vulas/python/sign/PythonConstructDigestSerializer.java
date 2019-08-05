package com.sap.psr.vulas.python.sign;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * <p>PythonConstructDigestSerializer class.</p>
 *
 */
public class PythonConstructDigestSerializer extends StdSerializer<PythonConstructDigest> {

	/**
	 * <p>Constructor for PythonConstructDigestSerializer.</p>
	 */
	public PythonConstructDigestSerializer() {
		this(null);
	}

	/**
	 * <p>Constructor for PythonConstructDigestSerializer.</p>
	 *
	 * @param t a {@link java.lang.Class} object.
	 */
	public PythonConstructDigestSerializer(Class<PythonConstructDigest> t) {
		super(t);
	}
	

	/** {@inheritDoc} */
	@Override
	public void serialize(PythonConstructDigest value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("computedFrom", value.getComputedFrom());
		jgen.writeStringField("computedFromType", value.getComputedFromType().toString());
		jgen.writeStringField("digest", value.getDigest());
		jgen.writeStringField("digestAlgorihtm", value.getDigestAlgorithm().toString());
		jgen.writeEndObject();
	}

}
