package com.sap.psr.vulas.kb.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import com.sap.psr.vulas.kb.context.Context;

public class CommandManagerTest {
  @Test
  public void testRunCommand() throws ParseException {
    BugIdentifier cmd = new BugIdentifier();
    CommandManager cmdManager = new CommandManager(cmd);


    final CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption(cmd.getCommandOption());

    String args = "-b CVE-1000";
    CommandLine cmdLine = parser.parse(options, args.split(" "));
    cmdManager.runCommand(cmdLine, new Context());
  }

  @Test(expected = ParseException.class)
  public void testRunCommandInvalidParams() throws ParseException {
    BugIdentifier cmd = new BugIdentifier();
    CommandManager cmdManager = new CommandManager(cmd);


    final CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption(cmd.getCommandOption());

    String args = "-z CVE-1000";
    CommandLine cmdLine = parser.parse(options, args.split(" "));
    cmdManager.runCommand(cmdLine, new Context());
  }
}
