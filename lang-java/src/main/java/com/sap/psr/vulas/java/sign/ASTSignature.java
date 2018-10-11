package com.sap.psr.vulas.java.sign;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaDeclarationConverter;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaMethodBodyConverter;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.sign.Signature;

public abstract class ASTSignature extends Node implements Signature {

	public ASTSignature(EntityType label, String value) {
		super(label, value);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -3802437501302095999L;
	protected static JavaDeclarationConverter sDeclarationConverter;
    protected static JavaMethodBodyConverter sMethodBodyConverter;   //Visitor for generation of the AST of construct bodies
	protected static final Injector sInjector = Guice.createInjector(new JavaChangeDistillerModule());
    protected JavaCompilation fCompilation;
    protected Node fRoot;
    protected Construct _construct;

    /**
     * Prints this Node and its children with <code>value ['{' child [, child]* '}']</code>.
     * */
    protected String getTreeString() {
        return fRoot.print(new StringBuilder()).toString();
    }

    public Node getFirstLeaf() {
        return ((Node) fRoot.getFirstLeaf());
    }

    public Node getFirstChild() {
        return (Node) fRoot.getFirstChild();
    }

    public Node getLastChild() {
        return (Node) fRoot.getLastChild();
    }

    public void createRootNode(EntityType label, String value) {
        fRoot = new Node(label, value);
        fRoot.setEntity(new SourceCodeEntity(value, label, new SourceRange()));
    }

    protected abstract String getSourceCodeWithSnippets(String... snippets);

    public Node getRoot(){
    	return (Node) this.fRoot;
    }

    public void setRoot(Node n){
    	fRoot = n;
    }
}
