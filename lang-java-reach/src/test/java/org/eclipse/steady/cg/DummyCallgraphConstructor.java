package org.eclipse.steady.cg;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.eclipse.steady.cg.spi.ICallgraphConstructor;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.util.VulasConfiguration;

import com.ibm.wala.util.graph.Graph;

public class DummyCallgraphConstructor implements ICallgraphConstructor {
  public String getFramework() {
    return "dummy";
  }

  public void setAppClasspath(String _cp) {}

  public void setDepClasspath(String _dependencyClasspath) {}

  public void setExcludePackages(String _packages) {}

  public Set<ConstructId> getEntrypoints() {
    return null;
  }

  public void buildCallgraph(boolean _policy) throws CallgraphConstructException {}

  public long getConstructionTime() {
    return 0L;
  }

  public Configuration getConfiguration() {
    return null;
  }

  public Graph<ConstructId> getCallgraph() {
    return null;
  }

  public void setEntrypoints(Set<ConstructId> _constructs) throws CallgraphConstructException {}

  public void setAppContext(Application _ctx) {}

  /** {@inheritDoc} */
  public void setVulasConfiguration(VulasConfiguration _cfg) {}

  public Configuration getConstructorConfiguration() {
    return null;
  }
}
