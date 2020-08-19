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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.core.util.SignatureConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.IUniqueNameNormalizer;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

/**
 * <p>UniqueNameNormalizer class.</p>
 *
 */
public class UniqueNameNormalizer implements IUniqueNameNormalizer {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final Pattern CONSTANT_PATTERN =
      Pattern.compile("([0-9a-zA-Z_\\.]+)\\.([0-9A-Z_]+)");

  private static UniqueNameNormalizer instance = null;

  private ClassLoader classLoader = null;

  private static String cua = null; // class under analysis

  private UniqueNameNormalizer() {}

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.java.sign.UniqueNameNormalizer} object.
   */
  public static synchronized UniqueNameNormalizer getInstance() {
    if (instance == null) instance = new UniqueNameNormalizer();
    return instance;
  }

  /**
   * Fully qualified class names to be searched for class names found in strings.
   */
  private final Set<String> classNames = new HashSet<String>();
  /**
   * <p>addStrings.</p>
   *
   * @param _class_names a {@link java.util.Collection} object.
   */
  public final void addStrings(Collection<String> _class_names) {
    classNames.addAll(_class_names);
  }
  /**
   * <p>addStrings.</p>
   *
   * @param _class_names an array of {@link java.lang.String} objects.
   */
  public final void addStrings(String[] _class_names) {
    classNames.addAll(Arrays.asList(_class_names));
  }
  /**
   * <p>addConstructIds.</p>
   *
   * @param _classes a {@link java.util.Collection} object.
   */
  public final void addConstructIds(Collection<ConstructId> _classes) {
    for (ConstructId cid : _classes) {
      classNames.add(cid.getQualifiedName());
    }
  }

  /**
   * Sets the class loader used for inlining constants, see {@link UniqueNameNormalizer#findConstants(String, String)}.
   *
   * @param _cl a {@link java.lang.ClassLoader} object.
   */
  public void setClassLoader(ClassLoader _cl) {
    this.classLoader = _cl;
  }

  /** {@inheritDoc} */
  @Override
  public boolean haveEqualUniqueName(SourceCodeEntity _e1, SourceCodeEntity _e2) {
    // Equality before normalization
    final boolean eq_before_norm = _e1.getUniqueName().equals(_e2.getUniqueName());

    // Equality after normalization
    final String p1 = this.normalizeUniqueName(_e1);
    final String p2 = this.normalizeUniqueName(_e2);
    final boolean eq_after_norm = p1.equals(p2);

    // Print log message in case the pre-processing realized a match that did not exist before
    if (!eq_before_norm && eq_after_norm) { // && !_e1.equals(_e2)) {
      UniqueNameNormalizer.log.info("Preprocessor match: Old [" + _e1 + "] and [" + _e2 + "]");
      UniqueNameNormalizer.log.info("                    New [" + p1 + "] and [" + p2 + "]");
    }
    return eq_after_norm;
  }

  /**
   * Applies changes that are independent of a given {@link EntityType} to the given {@link String}.
   * In more detail, the string is trimmed, occurrences of "this." are removed and constants are inlined.
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String normalizeUniqueName(String _string) {
    String tmp = _string.trim();
    tmp = tmp.replaceAll("this\\.", "");
    tmp = tmp.replaceAll("(Object)", "");
    if (VulasConfiguration.getGlobal()
        .getConfiguration()
        .getBoolean(SignatureConfiguration.RELAX_STRIP_FINALS)) {
      tmp = tmp.replaceAll("final ", "");
    }
    // TODO: Replace single characters 'x' by ASCII codes (cf. CVE-2009-2625) as done by some
    // compilers
    tmp = this.inlineConstants(tmp);
    return tmp;
  }

  /**
   * {@inheritDoc}
   *
   * Applies changes to the unique name of the given {@link SourceCodeEntity}, potentially dependent
   * on its specific {@link EntityType}.
   */
  @Override
  public String normalizeUniqueName(SourceCodeEntity _entity) {
    String toFix = _entity.getUniqueName();
    // get rid of class name for static methods
    if (_entity.getType().isStatement() && cua != null) {
      toFix = removeLeadingClassName(toFix);
    }
    // Normalizations applicable to all entities
    String tmp = this.normalizeUniqueName(toFix);
    tmp = this.removeNumberCasts(tmp);
    // Normalizations specific to entity types

    // Variable declaration: Remove leading "final"

    return tmp;
  }

  /**
   * <p>removeLeadingClassName.</p>
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String removeLeadingClassName(String _string) {
    String tmp = _string.trim();
    tmp = tmp.replaceAll(cua + "\\.", "");
    return tmp;
  }

  /**
   * <p>removeNumberCasts.</p>
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String removeNumberCasts(String _string) {
    String regex_string = "[^\"]*\"[^\"]*\"";
    Pattern pattern_string = Pattern.compile(regex_string);
    Matcher matcher_string = pattern_string.matcher(_string);
    // do not substitute when is a user defined string
    if (!matcher_string.matches()) {
      String regex = "([^0-9]*)([0-9]+)([BDFL])(.*)";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(_string);
      final StringBuilder tmp = new StringBuilder();
      int i = 0;
      while (matcher.find()) {
        tmp.append(matcher.group(1)).append(matcher.group(2)).append(matcher.group(4));
        i = matcher.end();
      }
      return tmp.toString();
    } else {
      return _string;
    }
  }

  /**
   * <p>inlineConstants.</p>
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String inlineConstants(String _string) {
    final StringBuilder b = new StringBuilder();
    final Matcher m = CONSTANT_PATTERN.matcher(_string);
    int idx = 0;
    String constant_name = null, constant_value = null;
    Set<Field> fields = null;
    while (m.find()) {
      constant_name = _string.substring(m.start(), m.end());

      // Find fields and take value
      fields = this.findConstants(m.group(1), m.group(2));
      if (fields.size() == 1) constant_value = this.getConstantValue(fields.iterator().next());
      else if (fields.size() > 1) {
        UniqueNameNormalizer.log.warn(
            fields.size() + " constants [" + constant_name + "] found, take first");
        constant_value = this.getConstantValue(fields.iterator().next());
      } else constant_value = null;

      b.append(_string.substring(idx, m.start()));
      if (constant_value == null) b.append(_string.substring(m.start(), m.end()));
      else b.append(constant_value);
      idx = m.end();
    }
    b.append(_string.substring(idx));
    return b.toString();
  }

  /**
   * Returns the value of the given {@link Field} as {@link String}.
   *
   * Todo: Until now, the method only distinguishes primitive types and
   * everything else. In both cases, the value is obtained by calling
   * toString. In the latter case, the value is additionally wrapped in quotes.
   * One must probably distinguish further cases, e.g., chars wrapper by single quotes.
   *
   * @param _field
   * @return
   */
  private String getConstantValue(Field _field) {
    final StringBuilder value = new StringBuilder();
    try {
      final Class type = _field.getType();
      _field.setAccessible(true);
      if (type.isPrimitive()) {
        value.append(_field.get(null).toString());
      } else {
        value.append("\"").append(_field.get(null).toString()).append("\"");
      }
    } catch (IllegalArgumentException e) {
      UniqueNameNormalizer.log.error(
          "Error while obtaining value of field [" + _field + "]: " + e.getMessage(), e);
    } catch (IllegalAccessException e) {
      UniqueNameNormalizer.log.error(
          "Error while accessing value of field [" + _field + "]: " + e.getMessage(), e);
    } catch (NoClassDefFoundError e2) {
    }
    return value.toString();
  }

  /**
   * Searches for classes or interfaces of the given name (w/o package qualifier) and which have a constant of the given name.
   *
   * Considers all classes before passed to {@link UniqueNameNormalizer#addStrings(Collection)} or the other two methods.
   * At the same time, the classes must be in the class path so that they can be loaded.
   *
   * @param _class_name
   * @param _field_name
   * @return
   */
  private Set<Field> findConstants(String _class_name, String _field_name) {

    // Find all classes
    final Set<Class> classes = new HashSet<Class>();
    // Replace . in class names by $
    final String class_name = _class_name.replaceAll("\\.", "\\$");
    Class cl = null;
    for (String qn : this.classNames) {
      if (qn.indexOf(class_name) != -1) {

        // If existing, try the member class loader (e.g., set by the Maven plugin)
        if (this.classLoader != null) {
          try {
            cl = this.classLoader.loadClass(qn);
          } catch (ClassNotFoundException e) {
          } catch (NoClassDefFoundError e2) {
          }
        }

        // If not existing or unsuccessful, try the system class loader
        if (cl == null) {
          try {
            cl = Class.forName(qn);
          } catch (ClassNotFoundException e) {
          } catch (NoClassDefFoundError e2) {
          }
        }

        // Add if found, otherwise write error message
        if (cl == null)
          UniqueNameNormalizer.log.error(
              "Error instantiating class or interface ["
                  + qn
                  + "], needed for constant ["
                  + _class_name
                  + "."
                  + _field_name
                  + "]");
        else classes.add(cl);
      }
    }

    // For each class, try to find a constant with the given name
    final Set<Field> fields = new HashSet<Field>();
    Field f = null;
    int mod = -1;
    for (Class c : classes) {
      try {
        f = c.getDeclaredField(_field_name);
        mod = f.getModifiers();
        if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
          fields.add(f);
        }
      } catch (NoSuchFieldException e) {
      } catch (NoClassDefFoundError e) {
        UniqueNameNormalizer.log.error(
            "Class definition not found when searching for constant ["
                + _field_name
                + "] in class or interface ["
                + c.getName()
                + "]: "
                + e.getMessage());
      } catch (SecurityException e) {
        UniqueNameNormalizer.log.error(
            "Security exception when searching for constant ["
                + _field_name
                + "] in class or interface ["
                + c.getName()
                + "]: "
                + e.getMessage(),
            e);
      }
    }
    return fields;
  }

  /**
   * <p>setClassUnderAnalysisName.</p>
   *
   * @param _name a {@link java.lang.String} object.
   */
  public void setClassUnderAnalysisName(String _name) {
    cua = _name;
  }
}
