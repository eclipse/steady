package com.sap.psr.vulas.java.sign;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.java.sign.gson.ASTConstructBodySignatureSerializer;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.sign.Signature;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaMethodBodyConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * <p>ASTConstructBodySignature class.</p>
 *
 */
public class ASTConstructBodySignature extends ASTSignature {

		private static final long serialVersionUID = 2722815156114326441L;

	    /**
	     * <p>Constructor for ASTConstructBodySignature.</p>
	     *
	     * @param _construct a {@link com.sap.psr.vulas.Construct} object.
	     */
	    public ASTConstructBodySignature(Construct _construct){
	    	 super(JavaEntityType.METHOD, _construct.getId().getSimpleName());
		     sMethodBodyConverter = sInjector.getInstance(JavaMethodBodyConverter.class);
		     this._construct = _construct;
		 	 fCompilation = CompilationUtils.compileSource(getSourceCodeWithSnippets(_construct.getContent()));
	    }

	    //A Copy constructor :
	    //To make a copy of the defective construct, the "Node" object is mutable as implemented in changedistiller
	    /**
	     * <p>Constructor for ASTConstructBodySignature.</p>
	     *
	     * @param copy a {@link com.sap.psr.vulas.java.sign.ASTConstructBodySignature} object.
	     */
	    public ASTConstructBodySignature(ASTConstructBodySignature copy){
	    		this(copy._construct);
	    }


	    /**
	     * <p>Constructor for ASTConstructBodySignature.</p>
	     *
	     * @param fLabel a {@link ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType} object.
	     * @param fValue a {@link java.lang.String} object.
	     */
	    public ASTConstructBodySignature(EntityType fLabel, String fValue) {
	    	super(fLabel,fValue);
			// TODO Auto-generated constructor stub
		}

	   /**
	    * Returns the generated {@link JavaCompilation} from the file identified by the given filename. This method assumes
	    * that the filename is relative to <code>{@value #TEST_DATA_BASE_DIR}</code>.
	    *
	    * @param _file a {@link java.lang.String} object.
	    */
	   public void prepareCompilationFromFile(String _file) {
	        fCompilation = CompilationUtils.compileFile(_file);
	    }

	    /**
	     * <p>prepareCompilationFromSource.</p>
	     *
	     * @param src a {@link java.lang.String} object.
	     */
	    public void prepareCompilationFromSource(String src){
	    	fCompilation = CompilationUtils.compileSource(getSourceCodeWithSnippets(src));
	    }


	    /** {@inheritDoc} */
	    @Override
	    protected String getSourceCodeWithSnippets(String... snippets) {
	        StringBuilder src = new StringBuilder("public class Foo { ");
	        for (String statement : snippets) {
	            src.append(statement).append(' ');
	        }
	        src.append("} ");
	        return src.toString();
	    }


	   /**
	    * <p>convertConstructBody.</p>
	    *
	    * @param methodName a {@link java.lang.String} object.
	    * @return a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
	    */
	   public Node convertConstructBody(String methodName) {
	        AbstractMethodDeclaration method = CompilationUtils.findMethod(fCompilation.getCompilationUnit(), methodName);
	        fRoot = new Node(JavaEntityType.METHOD, methodName);
	        fRoot.setEntity(new SourceCodeEntity(methodName, JavaEntityType.METHOD, new SourceRange(
	                method.declarationSourceStart,
	                method.declarationSourceEnd)));

	        //List<Comment> comments = CompilationUtils.extractComments(fCompilation);

	        //Here initialize the visitor with parameters
	        //sMethodBodyConverter.initialize(fRoot, method, comments, fCompilation.getScanner());

	        //Removed the <<comments>> argument from the previous statement
	        sMethodBodyConverter.initialize(fRoot, method, null, fCompilation.getScanner());

	        //AbstractMethodDeclaration.traverse(ASTVisitor,ClassScope)
	        method.traverse(sMethodBodyConverter, (ClassScope) null);
	        return fRoot;
	    }



/**
 * @param n - The Root Node
 * @return - JSON representation of the AST
 *  NB : An ast as an array of Nodes
 *   A Node {"name" : "Value of Node", "parent" : "value of parent Node", "children" : [{"name" : "value of node", "parent" : "value of parent Node"},......,{}]}
 */

	   /*private String JSON(Node n, StringBuilder buffer){

	    	if(n.isLeaf()){
	    		buffer.append("{");
	    		buildJsonElement(n,buffer);
	    		buffer.append("}");
	    	}

	    	else{
	    		int y = n.getChildCount();
				int x = 0;
				for(int i=0; i < n.getChildCount(); i++)
				{
						Node node = (Node)n.getChildAt(i);
						buffer.append("{");
						buildJsonElement(node,buffer);


						if(!node.isLeaf()){
							buffer.append(",");
							buffer.append("\"C\" : [" );  //Children
							JSON(node,buffer);
							buffer.append("]"); //close off the children object
						}
							buffer.append("}");
						if(++x < y){
							buffer.append(",");
						}
						//else{buffer.append("]}");}
				}
	    	}
	    	return buffer.toString();
	    }*/

		 /**
		  * Helper method for building a JSON element of a Node in the AST
		  *
		  * @param json a {@link java.lang.String} object.
		  * @return a {@link com.sap.psr.vulas.sign.Signature} object.
		  */
		    /*private void buildJsonElement(Node n ,StringBuilder buffer){
		    	buffer.append("\"Value\" : " ).append(JsonBuilder.escape(n.getValue().toString())).append(",");
				buffer.append("\"SourceCodeEntity\" :{ " ) ; //open SourceCodeEntity json element
					// The unique name has the same information as Node.Value
					//buffer.append("\"UniqueName\" : " ).append(JsonBuilder.escape(n.getEntity().getUniqueName().toString())).append(","); ;
					buffer.append("\"Modifiers\" : \"" ).append(n.getEntity().getModifiers()).append("\","); //DO I really need this, seems to be always zero for every Element
					buffer.append("\"SourceRange\" : {" );
						buffer.append("\"Start\" : " ).append(n.getEntity().getSourceRange().getStart()).append(","); ;
						buffer.append("\"End\" : " ).append(n.getEntity().getSourceRange().getEnd()); ;
					buffer.append("}");
				buffer.append("},");//close off SourceCodeEntity json element
				buffer.append("\"EntityType\" : " ).append(JsonBuilder.escape(n.getEntity().getType().toString()));    //Entity Type
		    }*/
		 public Signature toASTSignature(String json){
			 return null;
		 }


		 /** {@inheritDoc} */
		 @Override
		 public String toJson(){
			 final Map<Class<?>, StdSerializer<?>> custom_serializers = new HashMap<Class<?>, StdSerializer<?>>();
			 custom_serializers.put(ASTConstructBodySignature.class, new ASTConstructBodySignatureSerializer());
			 return JacksonUtil.asJsonString(this, custom_serializers);
			 
			 /*StringBuilder buffer = new StringBuilder();
		    	buffer.append("{\"ast\":[ ");
		    	if(fRoot.isRoot()){
		    		buffer.append("{");
		    		buildJsonElement(fRoot,buffer);
		    		if(fRoot.isLeaf())
						buffer.append("}");
					else{
							buffer.append(",\"C\" : [" );
							JSON(fRoot,buffer);
							buffer.append("]}");
					}
		    	}

		    	buffer.append("]}"); //close off the ast array opened above
		    	return buffer.toString();*/
		    }


		/**
		 *   Returns an AST Representation suited for using the RTED, Robust Tree Edit Distance Algorithm
		 *   (http://www.inf.unibz.it/dis/projects/tree-edit-distance/documentation.php)
		 *   Format of Tree Structure : {root{node{node}}{node}...}
		 */
		private String treeRTED(Node n, StringBuffer buffer){

			if(n!= null){

				//Root Node
				if(n.isRoot()){
					buffer.append("{" +n.getValue().toString().replaceAll("\\s+", ""));
				}

				for (int i=0; i < n.getChildCount(); i++){
					Node node = (Node)n.getChildAt(i);
					//Remove the spaces from the name of node labels
					buffer.append("{" +node.getValue().toString().replaceAll("\\s+", ""));
					if(!node.isLeaf()){
						treeRTED(node,buffer);
						buffer.append("}");
					}
					else
						buffer.append("}}");
				}

			}
			return buffer.toString();
		}


		/**
		 * <p>toRTEDString.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String toRTEDString(){
			return treeRTED(fRoot, new StringBuffer());
		}
}
