/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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

/**
 * <p>Abstract ASTSignature class.</p>
 *
 */
public abstract class ASTSignature extends Node implements Signature {

	/**
	 * <p>Constructor for ASTSignature.</p>
	 *
	 * @param label a {@link ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public ASTSignature(EntityType label, String value) {
		super(label, value);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -3802437501302095999L;
	/** Constant <code>sDeclarationConverter</code> */
	protected static JavaDeclarationConverter sDeclarationConverter;
    /** Constant <code>sMethodBodyConverter</code> */
    protected static JavaMethodBodyConverter sMethodBodyConverter;   //Visitor for generation of the AST of construct bodies
	/** Constant <code>sInjector</code> */
	protected static final Injector sInjector = Guice.createInjector(new JavaChangeDistillerModule());
    protected JavaCompilation fCompilation;
    protected Node fRoot;
    protected Construct _construct;

    /**
     * Prints this Node and its children with <code>value ['{' child [, child]* '}']</code>.
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getTreeString() {
        return fRoot.print(new StringBuilder()).toString();
    }

    /**
     * <p>getFirstLeaf.</p>
     *
     * @return a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
     */
    public Node getFirstLeaf() {
        return ((Node) fRoot.getFirstLeaf());
    }

    /**
     * <p>getFirstChild.</p>
     *
     * @return a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
     */
    public Node getFirstChild() {
        return (Node) fRoot.getFirstChild();
    }

    /**
     * <p>getLastChild.</p>
     *
     * @return a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
     */
    public Node getLastChild() {
        return (Node) fRoot.getLastChild();
    }

    /**
     * <p>createRootNode.</p>
     *
     * @param label a {@link ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType} object.
     * @param value a {@link java.lang.String} object.
     */
    public void createRootNode(EntityType label, String value) {
        fRoot = new Node(label, value);
        fRoot.setEntity(new SourceCodeEntity(value, label, new SourceRange()));
    }

    /**
     * <p>getSourceCodeWithSnippets.</p>
     *
     * @param snippets a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected abstract String getSourceCodeWithSnippets(String... snippets);

    /**
     * <p>getRoot.</p>
     *
     * @return a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
     */
    public Node getRoot(){
    	return (Node) this.fRoot;
    }

    /**
     * <p>setRoot.</p>
     *
     * @param n a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
     */
    public void setRoot(Node n){
    	fRoot = n;
    }
}
