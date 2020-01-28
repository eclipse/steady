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
package com.sap.psr.vulas.shared.json;

import java.util.Map;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Helper class for (de)serializing objects with Jackson.
 */
public class JacksonUtil {
	
	/**
	 * Serializes the given object to JSON, thereby using no custom {@link StdSerializer}s and no view {@link Class}.
	 *
	 * @param _object a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String asJsonString(final Object _object) {
		return JacksonUtil.asJsonString(_object, null, null);
	}

	/**
	 * Serializes the given object to JSON, thereby using the given {@link StdSerializer}s and no view {@link Class}.
	 *
	 * @param _object a {@link java.lang.Object} object.
	 * @param _custom_serializers a {@link java.util.Map} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String asJsonString(final Object _object, final Map<Class<?>, StdSerializer<?>> _custom_serializers) {
		return JacksonUtil.asJsonString(_object, _custom_serializers, null);
	}
	
	/**
	 * Serializes the given object to JSON, thereby using the given {@link StdSerializer}s and the given view {@link Class}.
	 *
	 * @param _object a {@link java.lang.Object} object.
	 * @param _custom_serializers a {@link java.util.Map} object.
	 * @param _view a {@link java.lang.Class} object.
	 * @return a {@link java.lang.String} object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String asJsonString(final Object _object, final Map<Class<?>, StdSerializer<?>> _custom_serializers, Class _view) {
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
            
    		String jsonContent = null;
    		if(_view==null) {
    			jsonContent = mapper.writeValueAsString(_object);
    		}
    		else {
    			jsonContent = mapper.writerWithView(_view).writeValueAsString(_object);
    		}
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	/**
	 * Deserializes the given JSON, thereby using no custom {@link StdDeserializer}s.
	 *
	 * @param _json a {@link java.lang.String} object.
	 * @param _clazz a {@link java.lang.Class} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object asObject(final String _json, final Class<?> _clazz) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(_json, _clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
