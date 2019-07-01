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

        assertEquals(23, c2.size());
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
        final NodejsId item_list = new NodejsId(modu, NodejsId.Type.OBJECT, "item_list");
        final NodejsId item_list_len = new NodejsId(item_list, NodejsId.Type.FUNCTION, "len(arr)");
        final NodejsId item_list_it_hello = new NodejsId(item_list, NodejsId.Type.FUNCTION, "it_hello()");
        final NodejsId item_list_add = new NodejsId(item_list, NodejsId.Type.METHOD, "add(x)");
        final NodejsId item_list_get = new NodejsId(item_list, NodejsId.Type.METHOD, "get(idx)");
        final NodejsId garage = new NodejsId(modu, NodejsId.Type.OBJECT, "garage");
        final NodejsId garage_get = new NodejsId(garage, NodejsId.Type.METHOD, "get@car()");
        final NodejsId garage_set = new NodejsId(garage, NodejsId.Type.METHOD, "set@car(c)");

        assertEquals(18, c1.size());
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
        assertTrue(c1.containsKey(item_list));
        assertTrue(c1.containsKey(item_list_len));
        assertTrue(c1.containsKey(item_list_it_hello));
        assertTrue(c1.containsKey(item_list_add));
        assertTrue(c1.containsKey(item_list_get));
        assertTrue(c1.containsKey(garage));
        assertTrue(c1.containsKey(garage_get));
        assertTrue(c1.containsKey(garage_set));


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
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/class_test.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final NodejsId pack1 = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu1 = new NodejsId(pack1, NodejsId.Type.MODULE, "class_test");
        final NodejsId pet = new NodejsId(modu1, NodejsId.Type.CLASS, "Pet()");
        final NodejsId pet_con = new NodejsId(pet, NodejsId.Type.CONSTRUCTOR, "constructor(name,owner)");
        final NodejsId dog = new NodejsId(modu1, NodejsId.Type.CLASS,"Dog(Pet)");
        final NodejsId dog_con = new NodejsId(dog, NodejsId.Type.CONSTRUCTOR, "constructor(name,owner,color)");
        final NodejsId dog_bark = new NodejsId(dog, NodejsId.Type.METHOD, "bark()");
        final NodejsId dog_get_color = new NodejsId(dog, NodejsId.Type.METHOD, "get@color()");
        final NodejsId dog_set_color = new NodejsId(dog, NodejsId.Type.METHOD, "set@color(color)");
        final NodejsId bird = new NodejsId(modu1, NodejsId.Type.CLASS, "Bird(Pet)");
        final NodejsId bird_con = new NodejsId(bird, NodejsId.Type.CONSTRUCTOR, "constructor(name,owner,size)");
        final NodejsId bird_fly = new NodejsId(bird, NodejsId.Type.METHOD, "fly(z)");
        final NodejsId blank = new NodejsId(modu1, NodejsId.Type.CLASS, "Blank()");
        final NodejsId human = new NodejsId(modu1, NodejsId.Type.CLASS, "Human()");
        final NodejsId human_con = new NodejsId(human, NodejsId.Type.CONSTRUCTOR, "constructor(name,age)");
        final NodejsId human_walk = new NodejsId(human, NodejsId.Type.METHOD, "walk(x,y)");
        final NodejsId rabbit = new NodejsId(modu1, NodejsId.Type.CLASS, "Rabbit(Pet)");
        final NodejsId rabbit_con = new NodejsId(rabbit, NodejsId.Type.CONSTRUCTOR, "constructor(name,owner,color,age)");
        final NodejsId rabbit_jump = new NodejsId(rabbit, NodejsId.Type.METHOD, "jump()");

        assertEquals(19, c1.size());
        assertTrue(c1.containsKey(pack1));
        assertTrue(c1.containsKey(modu1));
        assertTrue(c1.containsKey(pet));
        assertTrue(c1.containsKey(pet_con));
        assertTrue(c1.containsKey(dog));
        assertTrue(c1.containsKey(dog_con));
        assertTrue(c1.containsKey(dog_bark));
        assertTrue(c1.containsKey(dog_get_color));
        assertTrue(c1.containsKey(dog_set_color));
        assertTrue(c1.containsKey(bird));
        assertTrue(c1.containsKey(bird_con));
        assertTrue(c1.containsKey(bird_fly));
        assertTrue(c1.containsKey(blank));
        assertTrue(c1.containsKey(human));
        assertTrue(c1.containsKey(human_con));
        assertTrue(c1.containsKey(human_walk));
        assertTrue(c1.containsKey(rabbit));
        assertTrue(c1.containsKey(rabbit_con));
        assertTrue(c1.containsKey(rabbit_jump));
    }

    @Test
    public void testAnonClass() throws FileAnalysisException {
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/anonymous_class.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final NodejsId pack = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu = new NodejsId(pack, NodejsId.Type.MODULE, "anonymous_class");
        final NodejsId human = new NodejsId(modu, NodejsId.Type.CLASS, "Human()");
        final NodejsId human_con = new NodejsId(human, NodejsId.Type.CONSTRUCTOR, "constructor(name,surname)");
        final NodejsId human_get_full = new NodejsId(human, NodejsId.Type.METHOD, "get@fullname()");
        final NodejsId human_get_name = new NodejsId(human, NodejsId.Type.METHOD, "get@name()");
        final NodejsId human_set_name = new NodejsId(human, NodejsId.Type.METHOD, "set@name(name)");
        final NodejsId human_get_surname = new NodejsId(human, NodejsId.Type.METHOD, "get@surname()");
        final NodejsId human_set_surname = new NodejsId(human, NodejsId.Type.METHOD, "set@surname(surname)");
        final NodejsId anon_c1 = new NodejsId(modu, NodejsId.Type.CLASS, "1(Human)");
        final NodejsId anon_c1_con = new NodejsId(anon_c1, NodejsId.Type.CONSTRUCTOR, "constructor(name,surname,univ,year)");
        final NodejsId anon_c1_get_univ = new NodejsId(anon_c1, NodejsId.Type.METHOD, "get@university()");
        final NodejsId anon_c1_set_univ = new NodejsId(anon_c1, NodejsId.Type.METHOD, "set@university(univ)");
        final NodejsId anon_c1_get_year = new NodejsId(anon_c1, NodejsId.Type.METHOD, "get@year()");
        final NodejsId anon_c1_set_year = new NodejsId(anon_c1, NodejsId.Type.METHOD, "set@year(year)");
        final NodejsId anon_c2 = new NodejsId(modu, NodejsId.Type.CLASS, "2()");

        assertEquals(16, c1.size());
        assertTrue(c1.containsKey(pack));
        assertTrue(c1.containsKey(modu));
        assertTrue(c1.containsKey(human));
        assertTrue(c1.containsKey(human_con));
        assertTrue(c1.containsKey(human_get_full));
        assertTrue(c1.containsKey(human_get_name));
        assertTrue(c1.containsKey(human_set_name));
        assertTrue(c1.containsKey(human_get_surname));
        assertTrue(c1.containsKey(human_set_surname));
        assertTrue(c1.containsKey(anon_c1));
        assertTrue(c1.containsKey(anon_c1_con));
        assertTrue(c1.containsKey(anon_c1_get_univ));
        assertTrue(c1.containsKey(anon_c1_set_univ));
        assertTrue(c1.containsKey(anon_c1_get_year));
        assertTrue(c1.containsKey(anon_c1_set_year));
        assertTrue(c1.containsKey(anon_c2));

    }

    @Test
    public void testAnonFunction() throws FileAnalysisException {
        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/test-helloworld/anonymous_func.js"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final NodejsId pack1 = new NodejsId(null, NodejsId.Type.PACKAGE, "test-helloworld");
        final NodejsId modu1 = new NodejsId(pack1, NodejsId.Type.MODULE, "anonymous_func");
        final NodejsId b_obj = new NodejsId(modu1, NodejsId.Type.OBJECT, "b");
        final NodejsId named_func = new NodejsId(b_obj, NodejsId.Type.FUNCTION, "named_func(anon,x,y)");
        final NodejsId anon1 = new NodejsId(modu1, NodejsId.Type.FUNCTION, "1(a,b,success,fail)");
        final NodejsId anon1_1 = new NodejsId(named_func, NodejsId.Type.FUNCTION, "1(res)");
        final NodejsId anon1_2 = new NodejsId(named_func, NodejsId.Type.FUNCTION, "2(err)");
        final NodejsId anon2 = new NodejsId(modu1, NodejsId.Type.FUNCTION, "2(a,b,success,fail)");

        assertEquals(8, c1.size());
        assertTrue(c1.containsKey(pack1));
        assertTrue(c1.containsKey(modu1));
        assertTrue(c1.containsKey(b_obj));
        assertTrue(c1.containsKey(named_func));
        assertTrue(c1.containsKey(anon1));
        assertTrue(c1.containsKey(anon1_1));
        assertTrue(c1.containsKey(anon1_2));
        assertTrue(c1.containsKey(anon2));

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