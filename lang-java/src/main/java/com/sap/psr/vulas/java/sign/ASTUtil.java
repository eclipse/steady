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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * Implements different methods for comparing nodes (incl. their descendents).
 */
public class ASTUtil {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  public static enum NODE_COMPARE_MODE {
    ENTITY_TYPE,
    VALUE
  };

  /**
   * Returns true if the nodes are equal with regard to parameter mode.
   *
   * @param _mode a {@link com.sap.psr.vulas.java.sign.ASTUtil.NODE_COMPARE_MODE} object.
   * @param _left a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
   * @param _right a {@link ch.uzh.ifi.seal.changedistiller.treedifferencing.Node} object.
   * @return a boolean.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static final boolean isEqual(
      @NotNull Node _left, @NotNull Node _right, @NotNull NODE_COMPARE_MODE _mode)
      throws IllegalArgumentException {
    boolean is_equal = true;
    final Enumeration enum_left = _left.depthFirstEnumeration();
    final Enumeration enum_right = _right.depthFirstEnumeration();
    Node node_left = null, node_right = null;

    // Traverse left tree and compare one by one
    while (enum_left.hasMoreElements()) {
      node_left = (Node) enum_left.nextElement();

      // Compare the nodes
      if (enum_right.hasMoreElements()) {
        node_right = (Node) enum_right.nextElement();

        if (_mode.equals(NODE_COMPARE_MODE.ENTITY_TYPE)) {
          if (!node_left.getLabel().equals(node_right.getLabel())) {
            is_equal = false;
          }
        } else if (_mode.equals(NODE_COMPARE_MODE.VALUE)) {
          if (!node_left.getValue().equals(node_right.getValue())) {
            is_equal = false;
          }
        } else {
          throw new IllegalArgumentException("Illegal comparison mode: [" + _mode + "]");
        }
      }
      // Right tree has less elements
      else {
        is_equal = false;
      }

      // Stop comparison upon first difference
      if (!is_equal) break;
    }

    // Equal until now but right tree has more nodes?
    if (is_equal && enum_right.hasMoreElements()) {
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
  public static final Set<Object> intersectSourceCodeChanges(
      Collection _a, Collection _b, boolean _relaxed) {
    SourceCodeEntity.setIgnoreSourceRange(true);
    if (_relaxed) SourceCodeEntity.setUniqueNamePreprocessor(UniqueNameNormalizer.getInstance());
    else SourceCodeEntity.setUniqueNamePreprocessor(null);

    final Set<Object> intersection = new HashSet<Object>();
    outer:
    for (Object o1 : _b) {
      int i = 0;
      for (Object o2 : _a) {
        if (o1.equals(o2)) {
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
  public static final Set<Object> intersectSourceCodeChanges(
      Collection _a, Collection _b, boolean _relaxed, String _cn) {
    UniqueNameNormalizer uniqueNN = UniqueNameNormalizer.getInstance();
    uniqueNN.setClassUnderAnalysisName(_cn);
    SourceCodeEntity.setIgnoreSourceRange(true);
    if (_relaxed) SourceCodeEntity.setUniqueNamePreprocessor(uniqueNN);
    else SourceCodeEntity.setUniqueNamePreprocessor(null);

    final Set<Object> intersection = new HashSet<Object>();
    outer:
    for (Object o1 : _b) {
      int i = 0;
      for (Object o2 : _a) {
        if (o1.equals(o2)) {
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
  public static ChangeType getChangeType(String changeType) {

    ChangeType type = null;

    if (changeType.equals("ADDING_ATTRIBUTE_MODIFIABILITY")) {
      type = ChangeType.ADDING_ATTRIBUTE_MODIFIABILITY;
    } else if (changeType.equals("ADDING_CLASS_DERIVABILITY")) {
      type = ChangeType.ADDING_CLASS_DERIVABILITY;
    } else if (changeType.equals("ADDING_METHOD_OVERRIDABILITY")) {
      type = ChangeType.ADDING_METHOD_OVERRIDABILITY;
    } else if (changeType.equals("ADDITIONAL_CLASS")) {
      type = ChangeType.ADDITIONAL_CLASS;
    } else if (changeType.equals("ADDITIONAL_FUNCTIONALITY")) {
      type = ChangeType.ADDITIONAL_FUNCTIONALITY;
    } else if (changeType.equals("ADDITIONAL_OBJECT_STATE")) {
      type = ChangeType.ADDITIONAL_OBJECT_STATE;
    } else if (changeType.equals("ALTERNATIVE_PART_DELETE")) {
      type = ChangeType.ALTERNATIVE_PART_DELETE;
    } else if (changeType.equals("ALTERNATIVE_PART_INSERT")) {
      type = ChangeType.ALTERNATIVE_PART_INSERT;
    } else if (changeType.equals("ATTRIBUTE_RENAMING")) {
      type = ChangeType.ATTRIBUTE_RENAMING;
    } else if (changeType.equals("ATTRIBUTE_TYPE_CHANGE")) {
      type = ChangeType.ATTRIBUTE_TYPE_CHANGE;
    } else if (changeType.equals("CLASS_RENAMING")) {
      type = ChangeType.CLASS_RENAMING;
    } else if (changeType.equals("COMMENT_DELETE")) {
      type = ChangeType.COMMENT_DELETE;
    } else if (changeType.equals("COMMENT_INSERT")) {
      type = ChangeType.COMMENT_INSERT;
    } else if (changeType.equals("COMMENT_MOVE")) {
      type = ChangeType.COMMENT_MOVE;
    } else if (changeType.equals("COMMENT_UPDATE")) {
      type = ChangeType.COMMENT_UPDATE;
    } else if (changeType.equals("CONDITION_EXPRESSION_CHANGE")) {
      type = ChangeType.CONDITION_EXPRESSION_CHANGE;
    } else if (changeType.equals("DECREASING_ACCESSIBILITY_CHANGE")) {
      type = ChangeType.DECREASING_ACCESSIBILITY_CHANGE;
    } else if (changeType.equals("DOC_DELETE")) {
      type = ChangeType.DOC_DELETE;
    } else if (changeType.equals("DOC_INSERT")) {
      type = ChangeType.DOC_INSERT;
    } else if (changeType.equals("DOC_UPDATE")) {
      type = ChangeType.DOC_UPDATE;
    } else if (changeType.equals("INCREASING_ACCESSIBILITY_CHANGE")) {
      type = ChangeType.INCREASING_ACCESSIBILITY_CHANGE;
    } else if (changeType.equals("METHOD_RENAMING")) {
      type = ChangeType.METHOD_RENAMING;
    } else if (changeType.equals("PARAMETER_DELETE")) {
      type = ChangeType.PARAMETER_DELETE;
    } else if (changeType.equals("PARAMETER_INSERT")) {
      type = ChangeType.PARAMETER_INSERT;
    } else if (changeType.equals("PARAMETER_ORDERING_CHANGE")) {
      type = ChangeType.PARAMETER_ORDERING_CHANGE;
    } else if (changeType.equals("PARAMETER_RENAMING")) {
      type = ChangeType.PARAMETER_RENAMING;
    } else if (changeType.equals("PARAMETER_TYPE_CHANGE")) {
      type = ChangeType.PARAMETER_TYPE_CHANGE;
    } else if (changeType.equals("PARENT_CLASS_CHANGE")) {
      type = ChangeType.PARENT_CLASS_CHANGE;
    } else if (changeType.equals("PARENT_CLASS_DELETE")) {
      type = ChangeType.PARENT_CLASS_DELETE;
    } else if (changeType.equals("PARENT_CLASS_INSERT")) {
      type = ChangeType.PARENT_CLASS_INSERT;
    } else if (changeType.equals("PARENT_INTERFACE_CHANGE")) {
      type = ChangeType.PARENT_INTERFACE_CHANGE;
    } else if (changeType.equals("PARENT_INTERFACE_DELETE")) {
      type = ChangeType.PARENT_INTERFACE_DELETE;
    } else if (changeType.equals("PARENT_INTERFACE_INSERT")) {
      type = ChangeType.PARENT_INTERFACE_INSERT;
    } else if (changeType.equals("REMOVED_CLASS")) {
      type = ChangeType.REMOVED_CLASS;
    } else if (changeType.equals("REMOVED_FUNCTIONALITY")) {
      type = ChangeType.REMOVED_FUNCTIONALITY;
    } else if (changeType.equals("REMOVED_OBJECT_STATE")) {
      type = ChangeType.REMOVED_OBJECT_STATE;
    } else if (changeType.equals("REMOVING_ATTRIBUTE_MODIFIABILITY")) {
      type = ChangeType.REMOVING_ATTRIBUTE_MODIFIABILITY;
    } else if (changeType.equals("REMOVING_CLASS_DERIVABILITY")) {
      type = ChangeType.REMOVING_CLASS_DERIVABILITY;
    } else if (changeType.equals("REMOVING_METHOD_OVERRIDABILITY")) {
      type = ChangeType.REMOVING_METHOD_OVERRIDABILITY;
    } else if (changeType.equals("RETURN_TYPE_CHANGE")) {
      type = ChangeType.RETURN_TYPE_CHANGE;
    } else if (changeType.equals("RETURN_TYPE_DELETE")) {
      type = ChangeType.RETURN_TYPE_DELETE;
    } else if (changeType.equals("RETURN_TYPE_INSERT")) {
      type = ChangeType.RETURN_TYPE_INSERT;
    } else if (changeType.equals("STATEMENT_DELETE")) {
      type = ChangeType.STATEMENT_DELETE;
    } else if (changeType.equals("STATEMENT_INSERT")) {
      type = ChangeType.STATEMENT_INSERT;
    } else if (changeType.equals("STATEMENT_ORDERING_CHANGE")) {
      type = ChangeType.STATEMENT_ORDERING_CHANGE;
    } else if (changeType.equals("STATEMENT_PARENT_CHANGE")) {
      type = ChangeType.STATEMENT_PARENT_CHANGE;
    } else if (changeType.equals("STATEMENT_UPDATE")) {
      type = ChangeType.STATEMENT_UPDATE;
    } else if (changeType.equals("UNCLASSIFIED_CHANGE")) {
      type = ChangeType.UNCLASSIFIED_CHANGE;
    } else {
      // Default
      type = ChangeType.UNCLASSIFIED_CHANGE;
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
  public static EntityType getJavaEntityType(String type) {
    EntityType entityType = null;

    if (type.equals("METHOD")) {
      entityType = JavaEntityType.METHOD;
    } else if (type.equals("ARGUMENTS")) {
      entityType = JavaEntityType.ARGUMENTS;
    } else if (type.equals("ARRAY_ACCESS")) {
      entityType = JavaEntityType.ARRAY_ACCESS;
    } else if (type.equals("ARRAY_CREATION")) {
      entityType = JavaEntityType.ARRAY_CREATION;
    } else if (type.equals("ARRAY_INITIALIZER")) {
      entityType = JavaEntityType.ARRAY_INITIALIZER;
    } else if (type.equals("ARRAY_TYPE")) {
      entityType = JavaEntityType.ARRAY_TYPE;
    } else if (type.equals("ASSERT_STATEMENT")) {
      entityType = JavaEntityType.ASSERT_STATEMENT;
    } else if (type.equals("ASSIGNMENT")) {
      entityType = JavaEntityType.ASSIGNMENT;
    } else if (type.equals("FIELD")) {
      entityType = JavaEntityType.FIELD;
    } else if (type.equals("BLOCK")) {
      entityType = JavaEntityType.BLOCK;
    } else if (type.equals("BLOCK_COMMENT")) {
      entityType = JavaEntityType.BLOCK_COMMENT;
    } else if (type.equals("BODY")) {
      entityType = JavaEntityType.BODY;
    } else if (type.equals("BOOLEAN_LITERAL")) {
      entityType = JavaEntityType.BOOLEAN_LITERAL;
    } else if (type.equals("BREAK_STATEMENT")) {
      entityType = JavaEntityType.BREAK_STATEMENT;
    } else if (type.equals("CAST_EXPRESSION")) {
      entityType = JavaEntityType.CAST_EXPRESSION;
    } else if (type.equals("CATCH_CLAUSE")) {
      entityType = JavaEntityType.CATCH_CLAUSE;
    } else if (type.equals("CATCH_CLAUSES")) {
      entityType = JavaEntityType.CATCH_CLAUSES;
    } else if (type.equals("CHARACTER_LITERAL")) {
      entityType = JavaEntityType.CHARACTER_LITERAL;
    } else if (type.equals("CLASS")) {
      entityType = JavaEntityType.CLASS;
    } else if (type.equals("CLASS_INSTANCE_CREATION")) {
      entityType = JavaEntityType.CLASS_INSTANCE_CREATION;
    } else if (type.equals("COMPILATION_UNIT")) {
      entityType = JavaEntityType.COMPILATION_UNIT;
    } else if (type.equals("CONDITIONAL_EXPRESSION")) {
      entityType = JavaEntityType.CONDITIONAL_EXPRESSION;
    } else if (type.equals("CONSTRUCTOR_INVOCATION")) {
      entityType = JavaEntityType.CONSTRUCTOR_INVOCATION;
    } else if (type.equals("CONTINUE_STATEMENT")) {
      entityType = JavaEntityType.CONTINUE_STATEMENT;
    } else if (type.equals("DO_STATEMENT")) {
      entityType = JavaEntityType.DO_STATEMENT;
    } else if (type.equals("ELSE_STATEMENT")) {
      entityType = JavaEntityType.ELSE_STATEMENT;
    } else if (type.equals("EMPTY_STATEMENT")) {
      entityType = JavaEntityType.EMPTY_STATEMENT;
    } else if (type.equals("FOREACH_STATEMENT")) {
      entityType = JavaEntityType.FOREACH_STATEMENT;
    } else if (type.equals("FIELD_ACCESS")) {
      entityType = JavaEntityType.FIELD_ACCESS;
    } else if (type.equals("FIELD_DECLARATION")) {
      entityType = JavaEntityType.FIELD_DECLARATION;
    } else if (type.equals("FINALLY")) {
      entityType = JavaEntityType.FINALLY;
    } else if (type.equals("FOR_STATEMENT")) {
      entityType = JavaEntityType.FOR_STATEMENT;
    } else if (type.equals("IF_STATEMENT")) {
      entityType = JavaEntityType.IF_STATEMENT;
    } else if (type.equals("INFIX_EXPRESSION")) {
      entityType = JavaEntityType.INFIX_EXPRESSION;
    } else if (type.equals("INSTANCEOF_EXPRESSION")) {
      entityType = JavaEntityType.INSTANCEOF_EXPRESSION;
    } else if (type.equals("JAVADOC")) {
      entityType = JavaEntityType.JAVADOC;
    } else if (type.equals("LABELED_STATEMENT")) {
      entityType = JavaEntityType.LABELED_STATEMENT;
    } else if (type.equals("LINE_COMMENT")) {
      entityType = JavaEntityType.LINE_COMMENT;
    } else if (type.equals("METHOD_DECLARATION")) {
      entityType = JavaEntityType.METHOD_DECLARATION;
    } else if (type.equals("METHOD_INVOCATION")) {
      entityType = JavaEntityType.METHOD_INVOCATION;
    } else if (type.equals("MODIFIER")) {
      entityType = JavaEntityType.MODIFIER;
    } else if (type.equals("MODIFIERS")) {
      entityType = JavaEntityType.MODIFIERS;
    } else if (type.equals("NULL_LITERAL")) {
      entityType = JavaEntityType.NULL_LITERAL;
    } else if (type.equals("NUMBER_LITERAL")) {
      entityType = JavaEntityType.NUMBER_LITERAL;
    } else if (type.equals("PARAMETERIZED_TYPE")) {
      entityType = JavaEntityType.PARAMETERIZED_TYPE;
    } else if (type.equals("PARAMETERS")) {
      entityType = JavaEntityType.PARAMETERS;
    } else if (type.equals("PARAMETER")) {
      entityType = JavaEntityType.PARAMETER;
    } else if (type.equals("POSTFIX_EXPRESSION")) {
      entityType = JavaEntityType.POSTFIX_EXPRESSION;
    } else if (type.equals("PREFIX_EXPRESSION")) {
      entityType = JavaEntityType.PREFIX_EXPRESSION;
    } else if (type.equals("PRIMITIVE_TYPE")) {
      entityType = JavaEntityType.PRIMITIVE_TYPE;
    } else if (type.equals("QUALIFIED_NAME")) {
      entityType = JavaEntityType.QUALIFIED_NAME;
    } else if (type.equals("QUALIFIED_TYPE")) {
      entityType = JavaEntityType.QUALIFIED_TYPE;
    } else if (type.equals("RETURN_STATEMENT")) {
      entityType = JavaEntityType.RETURN_STATEMENT;
    } else if (type.equals("ROOT_NODE")) {
      entityType = JavaEntityType.ROOT_NODE;
    } else if (type.equals("SIMPLE_NAME")) {
      entityType = JavaEntityType.SIMPLE_NAME;
    } else if (type.equals(" SINGLE_TYPE")) {
      entityType = JavaEntityType.SINGLE_TYPE;
    } else if (type.equals("STRING_LITERAL")) {
      entityType = JavaEntityType.STRING_LITERAL;
    } else if (type.equals("SUPER_INTERFACE_TYPES")) {
      entityType = JavaEntityType.SUPER_INTERFACE_TYPES;
    } else if (type.equals("SWITCH_STATEMENT")) {
      entityType = JavaEntityType.SWITCH_STATEMENT;
    } else if (type.equals("SWITCH_STATEMENT")) {
      entityType = JavaEntityType.SWITCH_STATEMENT;
    } else if (type.equals("SYNCHRONIZED_STATEMENT")) {
      entityType = JavaEntityType.SYNCHRONIZED_STATEMENT;
    } else if (type.equals("THEN_STATEMENT")) {
      entityType = JavaEntityType.THEN_STATEMENT;
    } else if (type.equals("THROW")) {
      entityType = JavaEntityType.THROW;
    } else if (type.equals("THROW_STATEMENT")) {
      entityType = JavaEntityType.THROW_STATEMENT;
    } else if (type.equals("TRY_STATEMENT")) {
      entityType = JavaEntityType.TRY_STATEMENT;
    } else if (type.equals("TYPE_PARAMETERS")) {
      entityType = JavaEntityType.TYPE_PARAMETERS;
    } else if (type.equals("TYPE_DECLARATION")) {
      entityType = JavaEntityType.TYPE_DECLARATION;
    } else if (type.equals("TYPE_LITERAL")) {
      entityType = JavaEntityType.TYPE_LITERAL;
    } else if (type.equals("TYPE_PARAMETER")) {
      entityType = JavaEntityType.TYPE_PARAMETER;
    } else if (type.equals("VARIABLE_DECLARATION_STATEMENT")) {
      entityType = JavaEntityType.VARIABLE_DECLARATION_STATEMENT;
    } else if (type.equals("WHILE_STATEMENT")) {
      entityType = JavaEntityType.WHILE_STATEMENT;
    } else if (type.equals("WILDCARD_TYPE")) {
      entityType = JavaEntityType.WILDCARD_TYPE;
    } else if (type.equals("FOR_INIT")) {
      entityType = JavaEntityType.FOR_INIT;
    } else if (type.equals("FOR_INCR")) {
      entityType = JavaEntityType.FOR_INCR;
    } else {
      // Default Entity Type
      entityType = JavaEntityType.METHOD;
    }

    return entityType;
  }
}
