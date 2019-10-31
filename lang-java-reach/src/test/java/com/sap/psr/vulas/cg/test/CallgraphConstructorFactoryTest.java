package com.sap.psr.vulas.cg.test;

import static org.junit.Assert.*;

import com.sap.psr.vulas.cg.spi.CallgraphConstructorFactory;
import com.sap.psr.vulas.cg.spi.ICallgraphConstructor;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.junit.Test;

public class CallgraphConstructorFactoryTest {

  static {
    VulasConfiguration.getGlobal()
        .setProperty("vulas.reach.cli.plugins.dir", "target/test-classes");
  }

  @Test
  public void getDummyCallgraphServiceFromPluginFolder() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("dummy", null, true);
    assertEquals(callgraphConstructor.getFramework(), "dummy");
    assertEquals(
        callgraphConstructor.getClass().getName(),
        "com.sap.psr.vulas.cg.DummyCallgraphConstructor");
    assertTrue(callgraphConstructor instanceof ICallgraphConstructor);
  }

  @Test
  public void getDummyCallgraphServiceFromClasspath() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("dummy", null, false);
    assertEquals(callgraphConstructor, null);
  }
}
