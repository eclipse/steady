package com.sap.psr.vulas.nodejs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Map;

import com.sap.psr.vulas.*;
import org.junit.Test;

public class NodejsFileAnalyzerTest {

    @Test
    public void testNodejsTestApp() throws FileAnalysisException {
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/simple.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        assertEquals(3, c1.size());

        final NodejsId pack = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu = new NodejsId(pack, NodejsId.Type.MODULE, "simple");
        final NodejsId func = new NodejsId(modu, NodejsId.Type.FUNCTION, "hello(x,y)");
        assertTrue(c1.containsKey(pack));
        assertTrue(c1.containsKey(modu));
        assertTrue(c1.containsKey(func));

        final FileAnalyzer f2 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/cat.js"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();

        assertEquals(22, c2.size());
    }

    @Test
    public void testFunctionDeclarationExpression() throws FileAnalysisException {
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/func_test.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final NodejsId pack = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu = new NodejsId(pack, NodejsId.Type.MODULE, "func_test");
        final NodejsId basic_func = new NodejsId(modu, NodejsId.Type.FUNCTION, "basic(a,b)");
        final NodejsId basic_zero = new NodejsId(modu, NodejsId.Type.FUNCTION, "basic_zero()");
        final NodejsId func_exp = new NodejsId(modu, NodejsId.Type.FUNCTION, "func_exp(a,b)");
        final NodejsId func_exp_zero = new NodejsId(modu, NodejsId.Type.FUNCTION, "func_exp_zero()");
        final NodejsId func_exp_empty = new NodejsId(modu, NodejsId.Type.FUNCTION, "func_exp_empty()");
        final NodejsId func_exp_arg = new NodejsId(modu, NodejsId.Type.FUNCTION, "func_exp_arg(name)");
        final NodejsId arrow_func = new NodejsId(modu, NodejsId.Type.FUNCTION, "arrow_func(a,b)");
        final NodejsId arrow_empty = new NodejsId(modu, NodejsId.Type.FUNCTION, "arrow_empty()");
        final NodejsId item_list_len = new NodejsId(modu, NodejsId.Type.FUNCTION, "item_list.len(arr)");
        final NodejsId item_list_it_hello = new NodejsId(modu, NodejsId.Type.FUNCTION, "item_list.it_hello()");

        assertEquals(12, c1.size());
        assertTrue(c1.containsKey(pack));
        assertTrue(c1.containsKey(modu));
        assertTrue(c1.containsKey(basic_func));
        assertTrue(c1.containsKey(basic_zero));
        assertTrue(c1.containsKey(func_exp));
        assertTrue(c1.containsKey(func_exp_zero));
        assertTrue(c1.containsKey(func_exp_empty));
        assertTrue(c1.containsKey(func_exp_arg));
        assertTrue(c1.containsKey(arrow_func));
        assertTrue(c1.containsKey(arrow_empty));
        assertTrue(c1.containsKey(item_list_len));
        assertTrue(c1.containsKey(item_list_it_hello));
    }

    @Test
    public void testClassDeclarationExpression() throws FileAnalysisException {
    }

    @Test
    public void testAnonClass() throws FileAnalysisException {

    }

    @Test
    public void testAnonFunction() throws FileAnalysisException {

    }

    @Test
    public void testPackageModule() throws FileAnalysisException{

    }
}