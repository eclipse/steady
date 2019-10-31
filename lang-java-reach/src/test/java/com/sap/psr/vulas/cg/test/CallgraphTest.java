package com.sap.psr.vulas.cg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.sap.psr.vulas.cg.AbstractGetPaths;
import com.sap.psr.vulas.cg.Callgraph;
import com.sap.psr.vulas.cg.DepthFirstGetPaths;
import com.sap.psr.vulas.cg.PrunedGraphGetPaths;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Test;

public class CallgraphTest {

  // Disable upload for JUnit tests
  static {
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
    VulasConfiguration.getGlobal().setProperty(CoreConfiguration.APP_CTX_GROUP, "examples");
    VulasConfiguration.getGlobal().setProperty(CoreConfiguration.APP_CTX_ARTIF, "examples");
    VulasConfiguration.getGlobal().setProperty(CoreConfiguration.APP_CTX_VERSI, "0.0");
  }

  private GoalContext getGoalContext() {
    final GoalContext ctx = new GoalContext();
    ctx.setApplication(new Application("foo", "bar", "0.0"));
    return ctx;
  }

  /**
   * Expected tested class: Callgraph Test 1: compute the distance Test 2: compute the shortest path
   * Test 3/4: compute all paths based on different algorithms
   */
  @Test
  public void callgraphTest() {
    // Manually build a call graph: 10 nodes and 14 edges
    Graph<com.sap.psr.vulas.shared.json.model.ConstructId> graph = SlowSparseNumberedGraph.make();
    ConstructId a = JavaId.toSharedType(JavaId.parseMethodQName("test.a()"));
    graph.addNode(a);
    ConstructId b = JavaId.toSharedType(JavaId.parseMethodQName("test.b()"));
    graph.addNode(b);
    ConstructId c = JavaId.toSharedType(JavaId.parseMethodQName("test.c()"));
    graph.addNode(c);
    ConstructId d = JavaId.toSharedType(JavaId.parseMethodQName("test.d()"));
    graph.addNode(d);
    ConstructId e = JavaId.toSharedType(JavaId.parseMethodQName("test.e()"));
    graph.addNode(e);
    ConstructId f = JavaId.toSharedType(JavaId.parseMethodQName("test.f()"));
    graph.addNode(f);
    ConstructId g = JavaId.toSharedType(JavaId.parseMethodQName("test.g()"));
    graph.addNode(g);
    ConstructId h = JavaId.toSharedType(JavaId.parseMethodQName("test.h()"));
    graph.addNode(h);
    ConstructId i = JavaId.toSharedType(JavaId.parseMethodQName("test.i()"));
    graph.addNode(i);
    ConstructId j = JavaId.toSharedType(JavaId.parseMethodQName("test.j()"));
    graph.addNode(j);
    graph.addEdge(a, b);
    graph.addEdge(a, c);
    graph.addEdge(b, d);
    graph.addEdge(b, e);
    graph.addEdge(b, f);
    graph.addEdge(c, f);
    graph.addEdge(c, g);
    graph.addEdge(e, h);
    graph.addEdge(f, i);
    graph.addEdge(h, i);
    graph.addEdge(h, j);
    graph.addEdge(i, j);
    graph.addEdge(g, j);
    graph.addEdge(i, e);

    Callgraph cg = new Callgraph(graph);

    // source = a; target = j;
    // Test 1: compute the distance
    Map<ConstructId, Integer> distance = cg.getDist(j);
    int dist = distance.get(a);
    assertEquals(dist, 3);

    // Test 2: compute the shortest path
    Map<ConstructId, LinkedList<Integer>> shortestPaths = cg.getShortestPath(j, null);
    LinkedList<ConstructId> expectedShortestPath = new LinkedList<ConstructId>();
    expectedShortestPath.add(c);
    expectedShortestPath.add(g);
    expectedShortestPath.add(j);
    LinkedList<Integer> spath = shortestPaths.get(a);
    LinkedList<ConstructId> computedShortestPath = new LinkedList<ConstructId>();
    if (spath != null) {
      for (int n = (spath.size() - 1); n >= 0; n--) {
        computedShortestPath.add(cg.getConstructForId(spath.get(n)));
      }
    }
    assertEquals(computedShortestPath, expectedShortestPath);

    // Test 3: compute all paths: DepthFirstGetPaths
    AbstractGetPaths getpaths = new DepthFirstGetPaths(cg.getGraph(), cg.getNodeId());
    HashSet<LinkedList<ConstructId>> paths = getpaths.getAllPaths(a, j);
    for (LinkedList<ConstructId> p : paths) {
      for (ConstructId cid : p) System.out.print(cid.getQname() + "		");
      System.out.println();
    }
    assertEquals(paths.size(), 7);

    // Test 4: compute all paths: Get all paths from pruned graph
    getpaths = new PrunedGraphGetPaths(cg.getGraph(), cg.getNodeId());
    paths = getpaths.getAllPaths(a, j);
    for (LinkedList<ConstructId> p : paths) {
      for (ConstructId cid : p) System.out.print(cid.getQname() + "		");
      System.out.println();
    }
    assertEquals(paths.size(), 7);
  }

  /*@Test
  public void examplesSootTest () throws CallgraphConstructException {
  	final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
  	ra.setCallGraphConstructor(AbstractConstructorFactory.getFactory("soot"));

  	// Set classpaths
  	final Set<Path> app_paths = new HashSet<Path>(), dep_paths = new HashSet<Path>();
  	app_paths.add(Paths.get("./src/test/resources/examples.jar"));
  	dep_paths.add(Paths.get("./src/test/resources/empty.jar"));
  	ra.setAppClasspaths(app_paths);
  	ra.setDependencyClasspaths(dep_paths);

  	// Set the EP manually
  	final Set<ConstructId> entrypoints = new HashSet<ConstructId>();
  	entrypoints.add(JavaId.toSharedType(JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Examples.main(String[])")));
  	ra.setEntryPoints(entrypoints, PathSource.A2C, false);
  	ra.setAppConstructs(entrypoints);

  	// Set the target constructs (manually, rather than using a bug)
  	final Map<String,Set<ConstructId>> target_constructs = new HashMap<String, Set<ConstructId>>();
  	final Set<ConstructId> changes = new HashSet<ConstructId>();
  	changes.add(JavaId.toSharedType(JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Cat.saySomething()")));
  	changes.add(JavaId.toSharedType(JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Fish.saySomething()")));
  	changes.add(JavaId.toSharedType(JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Dog.saySomething()")));
  	changes.add(JavaId.toSharedType(JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Car.saySomething()")));
  	target_constructs.put("does-not-exist", changes);
  	ra.setTargetConstructs(target_constructs);

  	try {
  		ReachabilityAnalyzer.startAnalysis(ra, 600000);
  	} catch (InterruptedException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  }*/

  //	@Test
  //	public void getChangesTest () {
  //		MavenId app = new MavenId("com.sap.research.security.vulas", "vulas-testapp",
  // "0.0.2-SNAPSHOT");
  //		ReachabilityAnalyzer ra = new ReachabilityAnalyzer(app, null);
  //		Map<String, Set<ConstructId>> changes = Collector.getInstance().getChangeList(app, null,
  // false);
  //		for(Map.Entry<String, Set<ConstructId>> entry : changes.entrySet()) {
  //			System.out.println("\r\nAll changes of bug [ " + entry.getKey() + " ]");
  //			for(ConstructId cid : entry.getValue()) System.out.println("--- " + cid.getQName());
  //		}
  //	}

  @Test
  public void testJarDigest() {
    final String qname = "java.lang.String";
    final JavaClassId cid = JavaId.parseClassQName(qname);
    final JarAnalyzer ja = new JarAnalyzer();
    try {
      final URL jar_url = cid.getJarUrl();
      final URI uri = jar_url.toURI();
      System.out.println("Jar URL [" + jar_url + "], URI [" + uri + "]");
      ja.analyze(Paths.get(uri).toFile());
      assertTrue(true);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }
}
