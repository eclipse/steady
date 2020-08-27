package org.eclipse.steady.kb.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.apache.commons.cli.Options;
import org.eclipse.steady.kb.command.CommandParser;
import org.eclipse.steady.kb.exception.CommandLineParserException;
import org.junit.Test;

public class CommandParserTest {

  private static final String UPLOAD_CONSTRUCT_OPTION = "u";
  private static final String DIRECTORY_OPTION = "d";
  private static final String OVERWRITE_OPTION = "o";
  private static final String VERBOSE_OPTION = "v";

  private static final String UPLOAD_LONG_OPTION = "upload";
  private static final String VERBOSE_LONG_OPTION = "verbose";
  private static final String OVERWRITE_LONG_OPTION = "overwrite";
  private static final String DIRECTORY_LONG_OPTION = "directory";

  @Test
  public void testParse() throws CommandLineParserException {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(
        OVERWRITE_OPTION,
        OVERWRITE_LONG_OPTION,
        false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(
        UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false, "Upload construct changes");

    String _args = "-d test -u -v";
    HashMap<String, Object> parsedCommands = CommandParser.parse(_args.split(" "), options);
    assertEquals("test", parsedCommands.get(DIRECTORY_OPTION));
    assertTrue((boolean) parsedCommands.get(VERBOSE_OPTION));
    assertTrue((boolean) parsedCommands.get(UPLOAD_CONSTRUCT_OPTION));
    assertFalse((boolean) parsedCommands.get(OVERWRITE_OPTION));
  }

  @Test(expected = CommandLineParserException.class)
  public void testRequiredOptions() throws CommandLineParserException {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(
        OVERWRITE_OPTION,
        OVERWRITE_LONG_OPTION,
        false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(
        UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false, "Upload construct changes");

    String _args = "-u -v";
    CommandParser.parse(_args.split(" "), options);
  }
}
