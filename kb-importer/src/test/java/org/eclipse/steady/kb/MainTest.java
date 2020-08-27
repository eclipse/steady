package org.eclipse.steady.kb;

import org.eclipse.steady.kb.Main;
import org.junit.Test;

public class MainTest {
  private static final String SPACE = " ";

  @Test
  public void testHelp() {
    String args = "help";
    Main.main(args.split(SPACE));
  }

  @Test
  public void testVersion() {
    String args = "version";
    Main.main(args.split(SPACE));
  }
}
