package com.sap.psr.vulas.shared.json;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Helper class for (de)serializing objects with Jackson.
 * 
 */
public class JacksonUtil {
	
	/**
	 * Serializes the given object to JSON, thereby using no custom {@link StdSerializer}s.
	 * @param _object
	 * @return
	 */
	public static String asJsonString(final Object _object) {
		return JacksonUtil.asJsonString(_object,  null);
	}

	/**
	 * Serializes the given object to JSON, thereby using the given {@link StdSerializer}s.
	 * @param _object
	 * @param _custom_serializers
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String asJsonString(final Object _object, final Map<Class<?>, StdSerializer<?>> _custom_serializers) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            
            // Register custom serializers
    		final SimpleModule module = new SimpleModule();
    		if(_custom_serializers!=null && !_custom_serializers.isEmpty()) {
    			for(Class clazz: _custom_serializers.keySet()) {
    				module.addSerializer(clazz, _custom_serializers.get(clazz));
    			}
    		}
    		mapper.registerModule(module);
            
            final String jsonContent = mapper.writeValueAsString(_object);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	/**
	 * Deserializes the given JSON, thereby using no custom {@link StdDeserializer}s.
	 * @param _object
	 * @param _custom_serializers
	 * @return
	 */
	public static Object asObject(final String json, final Class<?> clazz) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
