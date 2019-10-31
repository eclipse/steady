package com.sap.psr.vulas.java.sign;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * Implements different methods for comparing nodes (incl. their descendents).
 */
public class ASTUtil {

	private static final Log log = LogFactory.getLog(ASTUtil.class);

	public static enum NODE_COMPARE_MODE { ENTITY_TYPE, VALUE };

	/**
	 * Returns true if the nodes are equal with regard to parameter mode.
	 *
	 * @param _mode a {@link com.sap.psr.vulas.java.sign.ASTUtil.NODE_COMPARE_MODE} object.
	 * @param _left a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
	 * @param _right a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
	 * @return a boolean.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public static final boolean isEqual(@NotNull Node _left, @NotNull Node _right, @NotNull NODE_COMPARE_MODE _mode) throws IllegalArgumentException {
		boolean is_equal = true;
		final Enumeration enum_left  = _left.depthFirstEnumeration();
		final Enumeration enum_right = _right.depthFirstEnumeration();
		Node node_left = null, node_right = null;

		// Traverse left tree and compare one by one
		while(enum_left.hasMoreElements()) {
			node_left = (Node)enum_left.nextElement();

			// Compare the nodes
			if(enum_right.hasMoreElements()) {
				node_right = (Node)enum_right.nextElement();

				if(_mode.equals(NODE_COMPARE_MODE.ENTITY_TYPE)) {
					if(!node_left.getLabel().equals(node_right.getLabel())) {
						is_equal = false;
					}
				}
				else if(_mode.equals(NODE_COMPARE_MODE.VALUE)) {
					if(!node_left.getValue().equals(node_right.getValue())) {
						is_equal = false;
					}
				}
				else {
					throw new IllegalArgumentException("Illegal comparison mode: [" + _mode + "]");
				}
			}
			// Right tree has less elements
			else {
				is_equal = false;
			}

			// Stop comparison upon first difference
			if(!is_equal) break;
		}

		// Equal until now but right tree has more nodes?
		if(is_equal && enum_right.hasMoreElements()) {
			is_equal = false;
		}

		return is_equal;
	}

	/**
	 * <p>intersectSourceCodeChanges.</p>
	 *
	 * @param _a a {@link java.util.Collection} object.
	 * @param _b a {@link java.util.Collection} object.
	 * @param _relaxed a boolean.
	 * @return a {@link java.util.Set} object.
	 */
	public static final Set<Object> intersectSourceCodeChanges(Collection _a, Collection _b, boolean _relaxed) {
		SourceCodeEntity.setIgnoreSourceRange(true);
		if(_relaxed)
			SourceCodeEntity.setUniqueNamePreprocessor(UniqueNameNormalizer.getInstance());
		else
			SourceCodeEntity.setUniqueNamePreprocessor(null);

		final Set<Object> intersection = new HashSet<Object>();
		outer:
			for(Object o1: _b) {
				int i = 0;
				for(Object o2: _a) {
					if(o1.equals(o2)) {
						ASTUtil.log.info("                                   Added change [" + o1 + "]");
						intersection.add(o1);
						continue outer;
					}
				}
			}
		SourceCodeEntity.setIgnoreSourceRange(false);
		return intersection;
	}

	/**
	 * <p>intersectSourceCodeChanges.</p>
	 *
	 * @param _a a {@link java.util.Collection} object.
	 * @param _b a {@link java.util.Collection} object.
	 * @param _relaxed a boolean.
	 * @param _cn ClassName
	 * @return a {@link java.util.Set} object.
	 */
	public static final Set<Object> intersectSourceCodeChanges(Collection _a, Collection _b, boolean _relaxed, String _cn) {
		UniqueNameNormalizer uniqueNN = UniqueNameNormalizer.getInstance();
		uniqueNN.setClassUnderAnalysisName(_cn);
		SourceCodeEntity.setIgnoreSourceRange(true);
		if(_relaxed)
			SourceCodeEntity.setUniqueNamePreprocessor(uniqueNN);
		else
			SourceCodeEntity.setUniqueNamePreprocessor(null);

		final Set<Object> intersection = new HashSet<Object>();
		outer:
			for(Object o1: _b) {
				int i = 0;
				for(Object o2: _a) {
					if(o1.equals(o2)) {
						ASTUtil.log.info("                                   Added change [" + o1 + "]");
						intersection.add(o1);
						continue outer;
					}
				}
			}
		SourceCodeEntity.setIgnoreSourceRange(false);
		return intersection;
	}

	/**
	 *  Maps string representation of {@link ChangeType} to the corresponding enumeration value.
	 *
	 * @param changeType - String representation of changeType
	 * @return instance of ChangeType corresponding to the SourceCodeChange EntityType
	 */
	public static ChangeType getChangeType(String changeType){

		ChangeType type = null;

		switch (changeType) {
			case "ADDING_ATTRIBUTE_MODIFIABILITY":
				type = ChangeType.ADDING_ATTRIBUTE_MODIFIABILITY;
				break;
			case "ADDING_CLASS_DERIVABILITY":
				type = ChangeType.ADDING_CLASS_DERIVABILITY;
				break;
			case "ADDING_METHOD_OVERRIDABILITY":
				type = ChangeType.ADDING_METHOD_OVERRIDABILITY;
				break;
			case "ADDITIONAL_CLASS":
				type = ChangeType.ADDITIONAL_CLASS;
				break;
			case "ADDITIONAL_FUNCTIONALITY":
				type = ChangeType.ADDITIONAL_FUNCTIONALITY;
				break;
			case "ADDITIONAL_OBJECT_STATE":
				type = ChangeType.ADDITIONAL_OBJECT_STATE;
				break;
			case "ALTERNATIVE_PART_DELETE":
				type = ChangeType.ALTERNATIVE_PART_DELETE;
				break;
			case "ALTERNATIVE_PART_INSERT":
				type = ChangeType.ALTERNATIVE_PART_INSERT;
				break;
			case "ATTRIBUTE_RENAMING":
				type = ChangeType.ATTRIBUTE_RENAMING;
				break;
			case "ATTRIBUTE_TYPE_CHANGE":
				type = ChangeType.ATTRIBUTE_TYPE_CHANGE;
				break;
			case "CLASS_RENAMING":
				type = ChangeType.CLASS_RENAMING;
				break;
			case "COMMENT_DELETE":
				type = ChangeType.COMMENT_DELETE;
				break;
			case "COMMENT_INSERT":
				type = ChangeType.COMMENT_INSERT;
				break;
			case "COMMENT_MOVE":
				type = ChangeType.COMMENT_MOVE;
				break;
			case "COMMENT_UPDATE":
				type = ChangeType.COMMENT_UPDATE;
				break;
			case "CONDITION_EXPRESSION_CHANGE":
				type = ChangeType.CONDITION_EXPRESSION_CHANGE;
				break;
			case "DECREASING_ACCESSIBILITY_CHANGE":
				type = ChangeType.DECREASING_ACCESSIBILITY_CHANGE;
				break;
			case "DOC_DELETE":
				type = ChangeType.DOC_DELETE;
				break;
			case  "DOC_INSERT":
				type = ChangeType.DOC_INSERT;
				break;
			case "DOC_UPDATE":
				type = ChangeType.DOC_UPDATE;
				break;
			case "INCREASING_ACCESSIBILITY_CHANGE":
				type = ChangeType.INCREASING_ACCESSIBILITY_CHANGE;
				break;
			case "METHOD_RENAMING":
				type = ChangeType.METHOD_RENAMING;
				break;
			case "PARAMETER_DELETE":
				type = ChangeType.PARAMETER_DELETE;
				break;
			case "PARAMETER_INSERT":
				type = ChangeType.PARAMETER_INSERT;
				break;
			case "PARAMETER_ORDERING_CHANGE":
				type = ChangeType.PARAMETER_ORDERING_CHANGE;
				break;
			case "PARAMETER_RENAMING":
				type = ChangeType.PARAMETER_RENAMING;
				break;
			case "PARAMETER_TYPE_CHANGE":
				type = ChangeType.PARAMETER_TYPE_CHANGE;
				break;
			case "PARENT_CLASS_CHANGE":
				type = ChangeType.PARENT_CLASS_CHANGE;
				break;
			case "PARENT_CLASS_DELETE":
				type = ChangeType.PARENT_CLASS_DELETE;
				break;
			case "PARENT_CLASS_INSERT":
				type = ChangeType.PARENT_CLASS_INSERT;
				break;
			case "PARENT_INTERFACE_CHANGE":
				type = ChangeType.PARENT_INTERFACE_CHANGE;
				break;
			case "PARENT_INTERFACE_DELETE":
				type = ChangeType.PARENT_INTERFACE_DELETE;
				break;
			case "PARENT_INTERFACE_INSERT":
				type = ChangeType.PARENT_INTERFACE_INSERT;
				break;
			case "REMOVED_CLASS":
				type = ChangeType.REMOVED_CLASS;
				break;
			case "REMOVED_FUNCTIONALITY":
				type = ChangeType.REMOVED_FUNCTIONALITY;
				break;
			case "REMOVED_OBJECT_STATE":
				type = ChangeType.REMOVED_OBJECT_STATE;
				break;
			case "REMOVING_ATTRIBUTE_MODIFIABILITY":
				type = ChangeType.REMOVING_ATTRIBUTE_MODIFIABILITY;
				break;
			case "REMOVING_CLASS_DERIVABILITY":
				type = ChangeType.REMOVING_CLASS_DERIVABILITY;
				break;
			case  "REMOVING_METHOD_OVERRIDABILITY":
				type = ChangeType.REMOVING_METHOD_OVERRIDABILITY;
				break;
			case "RETURN_TYPE_CHANGE":
				type = ChangeType.RETURN_TYPE_CHANGE;
				break;
			case "RETURN_TYPE_DELETE":
				type = ChangeType.RETURN_TYPE_DELETE;
				break;
			case "RETURN_TYPE_INSERT":
				type = ChangeType.RETURN_TYPE_INSERT;
				break;
			case "STATEMENT_DELETE":
				type = ChangeType.STATEMENT_DELETE;
				break;
			case "STATEMENT_INSERT":
				type = ChangeType.STATEMENT_INSERT;
				break;
			case "STATEMENT_ORDERING_CHANGE":
				type  = ChangeType.STATEMENT_ORDERING_CHANGE;
				break;
			case "STATEMENT_PARENT_CHANGE":
				type = ChangeType.STATEMENT_PARENT_CHANGE;
				break;
			case "STATEMENT_UPDATE":
				type = ChangeType.STATEMENT_UPDATE;
				break;
			case  "UNCLASSIFIED_CHANGE":
				type = ChangeType.UNCLASSIFIED_CHANGE;
				break;
			default:
				//Default
				type = ChangeType.UNCLASSIFIED_CHANGE;
				break;
		}
		return type;

	}

	/**
	 * Helper method for mapping the JaveEntityTypes  for source code entities to string name of EntityType in the JSON
	 * TODO : It might be better to move this into a BaseClass, might also be used for deserializing SourceCodeChange
	 *
	 * @return Corresponding EntityType
	 *
	 * Number of SourceCodeEntityTypes
	 * @param type a {@link java.lang.String} object.
	 */
	public static EntityType getJavaEntityType (String type){
		EntityType entityType = null;

		switch(type) {
			case "METHOD":
				entityType =JavaEntityType.METHOD;
				break;
			case "ARGUMENTS":
				entityType =JavaEntityType.ARGUMENTS;
				break;
			case "ARRAY_ACCESS":
				entityType =JavaEntityType.ARRAY_ACCESS;
				break;
			case "ARRAY_CREATION":
				entityType =JavaEntityType.ARRAY_CREATION;
				break;
			case "ARRAY_INITIALIZER":
				entityType =JavaEntityType.ARRAY_INITIALIZER;
				break;
			case "ARRAY_TYPE":
				entityType =JavaEntityType.ARRAY_TYPE;
				break;
			case  "ASSERT_STATEMENT":
				entityType =JavaEntityType.ASSERT_STATEMENT;
				break;
			case  "ASSIGNMENT":
				entityType =JavaEntityType.ASSIGNMENT;
				break;
			case  "FIELD":
				entityType =JavaEntityType.FIELD;
				break;
			case  "BLOCK":
				entityType =JavaEntityType.BLOCK;
				break;
			case  "BLOCK_COMMENT":
				entityType =JavaEntityType.BLOCK_COMMENT;
				break;
			case  "BODY":
				entityType =JavaEntityType.BODY;
				break;
			case  "BOOLEAN_LITERAL":
				entityType =JavaEntityType.BOOLEAN_LITERAL;
				break;
			case  "BREAK_STATEMENT":
				entityType =JavaEntityType.BREAK_STATEMENT;
				break;
			case  "CAST_EXPRESSION":
				entityType =JavaEntityType.CAST_EXPRESSION;
				break;
			case  "CATCH_CLAUSE":
				entityType =JavaEntityType.CATCH_CLAUSE;
				break;
			case  "CATCH_CLAUSES":
				entityType =JavaEntityType.CATCH_CLAUSES;
				break;
			case  "CHARACTER_LITERAL":
				entityType =JavaEntityType.CHARACTER_LITERAL;
				break;
			case  "CLASS":
				entityType =JavaEntityType.CLASS;
				break;
			case  "CLASS_INSTANCE_CREATION":
				entityType =JavaEntityType.CLASS_INSTANCE_CREATION;
				break;
			case  "COMPILATION_UNIT":
				entityType =JavaEntityType.COMPILATION_UNIT;
				break;
			case  "CONDITIONAL_EXPRESSION":
				entityType =JavaEntityType.CONDITIONAL_EXPRESSION;
				break;
			case  "CONSTRUCTOR_INVOCATION":
				entityType =JavaEntityType.CONSTRUCTOR_INVOCATION;
				break;
			case  "CONTINUE_STATEMENT":
				entityType =JavaEntityType.CONTINUE_STATEMENT;
				break;
			case  "DO_STATEMENT":
				entityType =JavaEntityType.DO_STATEMENT;
				break;
			case  "ELSE_STATEMENT":
				entityType =JavaEntityType.ELSE_STATEMENT;
				break;
			case  "EMPTY_STATEMENT":
				entityType =JavaEntityType.EMPTY_STATEMENT;
				break;
			case  "FOREACH_STATEMENT":
				entityType =JavaEntityType.FOREACH_STATEMENT;
				break;
			case  "FIELD_ACCESS":
				entityType =JavaEntityType.FIELD_ACCESS;
				break;
			case  "FIELD_DECLARATION":
				entityType =JavaEntityType.FIELD_DECLARATION;
				break;
			case  "FINALLY":
				entityType =JavaEntityType.FINALLY;
				break;
			case  "FOR_STATEMENT":
				entityType =JavaEntityType.FOR_STATEMENT;
				break;
			case  "IF_STATEMENT":
				entityType =JavaEntityType.IF_STATEMENT;
				break;
			case  "INFIX_EXPRESSION":
				entityType =JavaEntityType.INFIX_EXPRESSION;
				break;
			case  "INSTANCEOF_EXPRESSION":
				entityType =JavaEntityType.INSTANCEOF_EXPRESSION;
				break;
			case  "JAVADOC":
				entityType =JavaEntityType.JAVADOC;
				break;
			case  "LABELED_STATEMENT":
				entityType =JavaEntityType.LABELED_STATEMENT;
				break;
			case  "LINE_COMMENT":
				entityType =JavaEntityType.LINE_COMMENT;
				break;
			case  "METHOD_DECLARATION":
				entityType =JavaEntityType.METHOD_DECLARATION;
				break;
			case  "METHOD_INVOCATION":
				entityType =JavaEntityType.METHOD_INVOCATION;
				break;
			case  "MODIFIER":
				entityType =JavaEntityType.MODIFIER;
				break;
			case  "MODIFIERS":
				entityType =JavaEntityType.MODIFIERS;
				break;
			case  "NULL_LITERAL":
				entityType =JavaEntityType.NULL_LITERAL;
				break;
			case  "NUMBER_LITERAL":
				entityType =JavaEntityType.NUMBER_LITERAL;
				break;
			case  "PARAMETERIZED_TYPE":
				entityType =JavaEntityType.PARAMETERIZED_TYPE;
				break;
			case  "PARAMETERS":
				entityType =JavaEntityType.PARAMETERS;
				break;
			case  "PARAMETER":
				entityType =JavaEntityType.PARAMETER;
				break;
			case  "POSTFIX_EXPRESSION":
				entityType =JavaEntityType.POSTFIX_EXPRESSION;
				break;
			case  "PREFIX_EXPRESSION":
				entityType =JavaEntityType.PREFIX_EXPRESSION;
				break;
			case  "PRIMITIVE_TYPE":
				entityType =JavaEntityType.PRIMITIVE_TYPE;
				break;
			case  "QUALIFIED_NAME":
				entityType =JavaEntityType.QUALIFIED_NAME;
				break;
			case  "QUALIFIED_TYPE":
				entityType =JavaEntityType.QUALIFIED_TYPE;
				break;
			case  "RETURN_STATEMENT":
				entityType =JavaEntityType.RETURN_STATEMENT;
				break;
			case  "ROOT_NODE":
				entityType =JavaEntityType.ROOT_NODE;
				break;
			case  "SIMPLE_NAME":
				entityType =JavaEntityType.SIMPLE_NAME;
				break;
			case  "SINGLE_TYPE":
				entityType =JavaEntityType.SINGLE_TYPE;
				break;
			case  "STRING_LITERAL":
				entityType =JavaEntityType.STRING_LITERAL;
				break;
			case  "SUPER_INTERFACE_TYPES":
				entityType =JavaEntityType.SUPER_INTERFACE_TYPES;
				break;
			case  "SWITCH_STATEMENT":
				entityType =JavaEntityType.SWITCH_STATEMENT;
				break;
			case  "SYNCHRONIZED_STATEMENT":
				entityType =JavaEntityType.SYNCHRONIZED_STATEMENT;
				break;
			case  "THEN_STATEMENT":
				entityType =JavaEntityType.THEN_STATEMENT;
				break;
			case  "THROW":
				entityType =JavaEntityType.THROW;
				break;
			case  "THROW_STATEMENT":
				entityType =JavaEntityType.THROW_STATEMENT;
				break;
			case  "TRY_STATEMENT":
				entityType =JavaEntityType.TRY_STATEMENT;
				break;
			case  "TYPE_PARAMETERS":
				entityType =JavaEntityType.TYPE_PARAMETERS;
				break;
			case  "TYPE_DECLARATION":
				entityType =JavaEntityType.TYPE_DECLARATION;
				break;
			case  "TYPE_LITERAL":
				entityType =JavaEntityType.TYPE_LITERAL;
				break;
			case  "TYPE_PARAMETER":
				entityType =JavaEntityType.TYPE_PARAMETER;
				break;
			case  "VARIABLE_DECLARATION_STATEMENT":
				entityType =JavaEntityType.VARIABLE_DECLARATION_STATEMENT;
				break;
			case  "WHILE_STATEMENT":
				entityType =JavaEntityType.WHILE_STATEMENT;
				break;
			case  "WILDCARD_TYPE":
				entityType =JavaEntityType.WILDCARD_TYPE;
				break;
			case  "FOR_INIT":
				entityType =JavaEntityType.FOR_INIT;
				break;
			case  "FOR_INCR":
				entityType =JavaEntityType.FOR_INCR;
				break;
			default:
				//Default Entity Type
				entityType =JavaEntityType.METHOD ;
				break;
		}
		return entityType;
	}
}
