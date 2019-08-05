package com.sap.psr.vulas.java.sign.gson;

import java.lang.reflect.Type;
import java.util.Set;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaClassInit;
import com.sap.psr.vulas.java.JavaConstructorId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.java.JavaPackageId;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;

/**
 * <p>GsonHelper class.</p>
 *
 */
public class GsonHelper {

	/**
	 * Returns a Vulas-specific GsonBuilder, i.e., with several custom serializers and deserializers registered.	 *
	 *
	 * @return a {@link com.google.gson.GsonBuilder} object.
	 */
	public static GsonBuilder getCustomGsonBuilder() {		
		final GsonBuilder gson = new GsonBuilder();

		// Subclasses of ConstructId and JavaId
		gson.registerTypeAdapter(ConstructId.class, new ConstructIdSerializer());
		gson.registerTypeAdapter(JavaPackageId.class, new ConstructIdSerializer());
		gson.registerTypeAdapter(JavaClassId.class, new ConstructIdSerializer());
		gson.registerTypeAdapter(JavaClassInit.class, new ConstructIdSerializer());
		gson.registerTypeAdapter(JavaConstructorId.class, new ConstructIdSerializer());
		gson.registerTypeAdapter(JavaMethodId.class, new ConstructIdSerializer());

		// Signature-related classes
		gson.registerTypeAdapter(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
		gson.registerTypeAdapter(ASTConstructBodySignature.class, new ASTSignatureDeserializer());

		return gson;
	}

	static class ConstructIdSerializer implements JsonSerializer<ConstructId>, JsonDeserializer<ConstructId> {

		public JsonElement serialize(ConstructId src, Type typeOfSrc, JsonSerializationContext context) {
			final JsonObject c = new JsonObject();
			c.addProperty("lang", src.getLanguage().toString());
			c.addProperty("type", JavaId.typeToString(((JavaId)src).getType()));
			c.addProperty("qname", src.getQualifiedName());
			final Set<String> annotations = ((JavaId)src).getAnnotations();
			if(!annotations.isEmpty()) {
				final JsonArray anno = new JsonArray();
				for(String a: annotations) {
					anno.add(new JsonPrimitive(a));
				}
				c.add("a", anno);
			}			
			return c;			
		}

		public ConstructId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			// To be returned
			ConstructId cid = null;

			final JsonObject c = (JsonObject)json;
			final String t  = c.getAsJsonPrimitive("type").getAsString();
			final String qn = c.getAsJsonPrimitive("qname").getAsString();

			if(JavaId.typeToString(JavaId.Type.PACKAGE).equals(t))
				cid = new JavaPackageId(qn);
			else if(JavaId.typeToString(JavaId.Type.CLASS).equals(t))
				cid = JavaId.parseClassQName(qn);
			else if(JavaId.typeToString(JavaId.Type.CLASSINIT).equals(t))
				cid = JavaId.parseClassInitQName(qn);
			else if(JavaId.typeToString(JavaId.Type.METHOD).equals(t))
				cid = JavaId.parseMethodQName(qn);
			else if(JavaId.typeToString(JavaId.Type.CONSTRUCTOR).equals(t))
				cid = JavaId.parseConstructorQName(qn);

			//TODO: Add annotations

			return cid;
		}
	}
}
