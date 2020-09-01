package org.eclipse.steady.kb.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.apache.commons.cli.Options;
import org.eclipse.steady.kb.exception.ValidationException;
import org.junit.Test;

public class ImportTest {
  @Test
  public void getOptions() {
    Command command = new Import();
    Options options = command.getOptions();
    assertEquals(options.getOptions().size(), 4);
    assertTrue(options.hasOption("d"));
    assertTrue(options.hasOption("u"));
    assertTrue(options.hasOption("v"));
    assertTrue(options.hasOption("o"));
  }

  @Test
  public void validate() throws ValidationException {
    Command command = new Import();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("d", ImportTest.class.getClassLoader().getResource("testRootDir1").getPath());
    command.validate(args);
  }

  @Test(expected = ValidationException.class)
  public void validationFail() throws ValidationException {
    Command command = new Import();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("d", "invalidDir");
    command.validate(args);
  }
}
