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

        final NodejsId pack = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu = new NodejsId(pack, NodejsId.Type.MODULE, "simple");
        final NodejsId func = new NodejsId(modu, NodejsId.Type.FUNCTION, "hello(x,y)");

        assertEquals(3, c1.size());
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

        /*
        final FileAnalyzer f2 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/antlr4-grammars-examples/ArrowFunctions.js"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();

        final NodejsId pack2 = new NodejsId(null, NodejsId.Type.PACKAGE, "antlr4-grammars-examples");
        final NodejsId modu2 = new NodejsId(pack2, NodejsId.Type.MODULE, "ArrowFunctions");
        final NodejsId odds = new NodejsId(modu2, NodejsId.Type.FUNCTION, "1(v)");
        final NodejsId pairs = new NodejsId(modu2, NodejsId.Type.FUNCTION, "2(v)");
        final NodejsId nums = new NodejsId(modu2, NodejsId.Type.FUNCTION, "3(v,i)");
        final NodejsId nums_each = new NodejsId(modu2, NodejsId.Type.FUNCTION, "4(v)");
        final NodejsId nums_each_this = new NodejsId(modu2, NodejsId.Type.FUNCTION, "5(v)");

        assertEquals(7, c2.size());
        assertTrue(c2.containsKey(pack2));
        assertTrue(c2.containsKey(modu2));
        assertTrue(c2.containsKey(odds));
        assertTrue(c2.containsKey(pairs));
        assertTrue(c2.containsKey(nums));
        assertTrue(c2.containsKey(nums_each));
        assertTrue(c2.containsKey(nums_each_this));
         */
    }

    @Test
    public void testClassDeclarationExpression() throws FileAnalysisException {
        /*
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/antlr4-grammars-examples/Classes.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        assertEquals(20, c1.size());
         */
    }

    @Test
    public void testAnonClass() throws FileAnalysisException {

    }

    @Test
    public void testAnonFunction() throws FileAnalysisException {
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/anonymous_func.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final NodejsId pack1 = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu1 = new NodejsId(pack1, NodejsId.Type.MODULE, "anonymous_func");
        final NodejsId named_func = new NodejsId(modu1, NodejsId.Type.FUNCTION, "b.named_func(anon,x,y)");
        final NodejsId anon1 = new NodejsId(modu1, NodejsId.Type.FUNCTION, "1(a,b,success,fail)");
        final NodejsId anon1_1 = new NodejsId(named_func, NodejsId.Type.FUNCTION, "1(res)");
        final NodejsId anon1_2 = new NodejsId(named_func, NodejsId.Type.FUNCTION, "2(err)");

        assertEquals(6, c1.size());
        assertTrue(c1.containsKey(pack1));
        assertTrue(c1.containsKey(modu1));
        assertTrue(c1.containsKey(named_func));
        assertTrue(c1.containsKey(anon1));
        assertTrue(c1.containsKey(anon1_1));
        assertTrue(c1.containsKey(anon1_2));

        /*
        final FileAnalyzer f2 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/callback_hell.js"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();

        final NodejsId pack2 = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu2 = new NodejsId(pack2, NodejsId.Type.MODULE, "callback_hell");
        final NodejsId anon_1 = new NodejsId(modu2, NodejsId.Type.FUNCTION, "1(err,files)");
        final NodejsId anon_2 = new NodejsId(anon_1, NodejsId.Type.FUNCTION, "1(filename,fileIndex)");
        final NodejsId anon_3 = new NodejsId(anon_2, NodejsId.Type.FUNCTION, "1(err,values)");
        final NodejsId anon_4 = new NodejsId(anon_3, NodejsId.Type.FUNCTION, "1(width,widthIndex)");
        final NodejsId anon_5 = new NodejsId(anon_4, NodejsId.Type.FUNCTION, "1(err)");

        assertEquals(7, c2.size());
        assertTrue(c2.containsKey(pack2));
        assertTrue(c2.containsKey(modu2));
        assertTrue(c2.containsKey(anon_1));
        assertTrue(c2.containsKey(anon_2));
        assertTrue(c2.containsKey(anon_3));
        assertTrue(c2.containsKey(anon_4));
        assertTrue(c2.containsKey(anon_5));
        */
    }

    @Test
    public void testPackageModule() throws FileAnalysisException{

    }
}