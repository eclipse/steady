package com.sap.psr.vulas.shared.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JsonBuilderTest {

  /** */
  @Test
  public void testJsonBuilder() {
    JsonBuilder b = new JsonBuilder();
    String pretty_json = null, json = null;
    b.startObject().startArrayProperty("array_property");
    b.startObject().appendObjectProperty("string_property", "string value").endObject();
    b.startObject().appendObjectProperty("int_property1", (Integer) null).endObject();
    b.startObject().appendObjectProperty("int_property2", new Integer(3)).endObject();
    b.endArray().endObject();

    pretty_json = b.getJson(true, "    ");
    assertEquals(
        "{\n    \"array_property\":[\n        {\n            \"string_property\":\"string value\"\n        },\n        {\n            \"int_property1\":null\n        },\n        {\n            \"int_property2\":3\n        }\n    ]\n}",
        pretty_json);

    json = b.getJson();
    assertEquals(
        "{\"array_property\":[{\"string_property\":\"string value\"},{\"int_property1\":null},{\"int_property2\":3}]}",
        json);
  }

  /** */
  @Test // l( expected = JsonSyntaxException.class)
  public void testSyntaxValidation() {
    final String invalid_json = "{ \"key1\" : \"value1\", }";
    // JsonBuilder.checkJsonValidity(invalid_json);
  }
}
