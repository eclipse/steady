package com.sap.psr.vulas.kb;

import org.junit.Test;

public class MainTest {
  private static final String SPACE = " ";

  @Test
  public void testMain() {
    String args = "-r https://github.com/FasterXML/jackson-databind " 
        + "-b CVE-2017-7525 ";
//        + "-d ./repo/1 "
//        + "-d ./repo/2"
//        + "-e e8f043d1aac9b82eee907e0f0c3abbdea723a935,ddfddfba6414adbecaff99684ef66eebd3a92e92,60d459cedcf079c6106ae7da2ac562bc32dcabe1 "
//        + "-links https://github.com/FasterXML/jackson-databind/issues/1599,https://github.com/FasterXML/jackson-databind/issues/1680,https://github.com/FasterXML/jackson-databind/issues/1737  "
//        + "-descr \"When configured to enable default typing, Jackson contained a deserialization vulnerability that could lead to arbitrary code execution. Jackson fixed this vulnerability by blacklisting known 'deserialization gadgets'. This vulnerability solves an incomplete fix for CVE-2017-4995-JK (main description at: https://github.com/FasterXML/jackson-databind/issues/1599 Issues not addressed by the incomplete fix of CVE-2017-4995-JK: https://github.com/FasterXML/jackson-databind/issues/1680 and https://github.com/FasterXML/jackson-databind/issues/1737) \"";
    Main.main(args.split(SPACE));
  }
}
