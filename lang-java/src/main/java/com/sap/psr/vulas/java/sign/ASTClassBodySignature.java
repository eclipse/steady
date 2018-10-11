package com.sap.psr.vulas.java.sign;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ASTClassBodySignature/* extends ASTSignature*/{

	private static final Log log = LogFactory.getLog(ASTClassBodySignature.class);

	 /*public ASTClassBodySignature(Construct _construct){
		 sDeclarationConverter = sInjector.getInstance(JavaDeclarationConverter.class);
	     this._construct = _construct;
	 	 fCompilation = CompilationUtils.compileSource(getSourceCodeWithSnippets(_construct.getContent()));
    }



	 @Override
	    protected String getSourceCodeWithSnippets(String... sourceSnippets) {
	        StringBuilder src = new StringBuilder("public class Foo { ");
	        for (String statement : sourceSnippets) {
	            src.append(statement).append(' ');
	        }
	        src.append("}");
	        return src.toString();
	    }



	 public Node convertMethodDeclaration(String methodName) {
	        AbstractMethodDeclaration method = CompilationUtils.findMethod(fCompilation.getCompilationUnit(), methodName);
	        fRoot = new Node(JavaEntityType.METHOD, methodName);
	        fRoot.setEntity(new SourceCodeEntity(methodName, JavaEntityType.METHOD, new SourceRange(
	                method.declarationSourceStart,
	                method.declarationSourceEnd)));
	        sDeclarationConverter.initialize(fRoot, fCompilation.getScanner());
	        method.traverse(sDeclarationConverter, (ClassScope) null);
	        return fRoot;
	    }


	 *//**
	  * Oops : Turns out JavaDeclarationConverter doesn't give us the AST of a java class instead only its declaration
	  *  You need to modify the JavaDeclarationConverter class in ChangeDistiller to have the AST
	  * @param className
	  * @return
	  *//*
    public Node convertClassDeclaration(String className) {
        //JavaCompilation compilation = CompilationUtils.compileSource(sourceCode);
        TypeDeclaration type = CompilationUtils.findType(fCompilation.getCompilationUnit(), className);
        fRoot = new Node(JavaEntityType.CLASS, className);
        fRoot.setEntity(new SourceCodeEntity(className, JavaEntityType.CLASS, new SourceRange(
                type.declarationSourceStart,
                type.declarationSourceEnd)));
        sDeclarationConverter.initialize(fRoot, fCompilation.getScanner());
        type.traverse(sDeclarationConverter, (ClassScope) null);
        return fRoot;
    }


    public void astTree(){

    }

	@Override
	public String toJson() {
		//For Now I am returning the simple string representation
		return getTreeString();
	}*/




}
