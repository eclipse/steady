package com.sap.psr.vulas.shared.json;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.FileUtil;

/**
 * Reads a serialized object from disk.
 *
 * @param <T>
 */
public class JsonReader<T> {
	
	private static final Log log = LogFactory.getLog(JsonReader.class);
	
	private Class<T> clazz;
	
	/**
	 * <p>Constructor for JsonReader.</p>
	 *
	 * @param _clazz a {@link java.lang.Class} object.
	 */
	public JsonReader(Class<T> _clazz) {
		this.clazz = _clazz;
	}
	
	/**
	 * <p>read.</p>
	 *
	 * @param _path a {@link java.nio.file.Path} object.
	 * @return a T object.
	 */
	@SuppressWarnings("unchecked")
	public T read(Path _path) {
		T object = null;
		if(FileUtil.isAccessibleFile(_path)) {
			try {
				final String json = FileUtil.readFile(_path);
				object = (T)JacksonUtil.asObject(json, this.clazz);
			} catch (IOException e) {
				log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
			} catch (ClassCastException e) {
				log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
			} catch (Exception e) {
				log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
			}
		}
		return object;
	}	
}
