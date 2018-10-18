import com.sap.psr.vulas.cg.ReachabilityAnalyzer;
import com.sap.psr.vulas.cg.soot.SootCallgraphConstructor;
import com.sap.psr.vulas.cg.spi.CallgraphConstructorFactory;
import com.sap.psr.vulas.cg.spi.ICallgraphConstructor;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.junit.Test;
import soot.SootClass;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SootCallGraphTest {

	static{
		VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
	}
	
    private GoalContext getGoalContext() {
        final GoalContext ctx = new GoalContext();
        ctx.setApplication(new Application("foo", "bar", "0.0"));
        return ctx;
    }


    private void runSootAnalysis() {
        final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
        ra.setCallgraphConstructor("soot", false);
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
        final Map<String, Set<ConstructId>> target_constructs = new HashMap<String, Set<ConstructId>>();
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
    }

    @Test
    public void callgraphServiceRegistered() {
        ICallgraphConstructor callgraphConstructor = CallgraphConstructorFactory.buildCallgraphConstructor("soot", null, false);
        assertEquals(callgraphConstructor.getFramework(), "soot");
        assertEquals(callgraphConstructor.getClass().getName(), SootCallgraphConstructor.class.getName());
        assertTrue(callgraphConstructor instanceof ICallgraphConstructor);
    }


    @Test
    public void examplesSootTestNoneEntrypointGenerator() {
        VulasConfiguration.getGlobal().setProperty("vulas.reach.soot.entrypointGenerator", "none");
        runSootAnalysis();


    }

    @Test
    public void examplesSootTestDefaultEntryPointGenerator() {
        VulasConfiguration.getGlobal().setProperty("vulas.reach.soot.entrypointGenerator", "soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator");
        runSootAnalysis();
    }

    @Test
    public void examplesSootTestCustomEntryPointGenerator() {
        VulasConfiguration.getGlobal().setProperty("vulas.reach.soot.entrypointGenerator", "com.sap.psr.vulas.cg.soot.CustomEntryPointCreator");
        runSootAnalysis();
    }


}
