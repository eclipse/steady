package com.sap.psr.vulas.shared.json;

import com.sap.psr.vulas.shared.util.FileUtil;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Serializes a given object and writes it to disk.
 *
 * @param <T>
 */
public class JsonWriter<T> {

  private static final Log log = LogFactory.getLog(JsonWriter.class);

  /**
   * write.
   *
   * @param _object a T object.
   * @param _path a {@link java.nio.file.Path} object.
   * @return a boolean.
   */
  public boolean write(T _object, Path _path) {
    boolean success = false;
    if (_object != null && _path != null) {
      // Serialize
      final String json = JacksonUtil.asJsonString(_object);
      // Write
      try {
        FileUtil.writeToFile(_path.toFile(), json);
        success = true;
      } catch (IOException e) {
        log.error("Error writing to file [" + _path + "]: " + e.getMessage(), e);
      }
    }
    return success;
  }
}
