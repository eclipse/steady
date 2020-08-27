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
package org.eclipse.steady.sign;

/**
 * A measure to express the similarity of constructs signatures. The similarity measure is used to understand whether different releases compared to each other are within the so-called stability range.
 */
public interface SignatureComparator {

  /**
   * Returns a value indicating the similarity of the two signatures provided as argument.
   * A value of 1 means that the signatures are identical, a value of 0 means that they do not have anything in common.
   *
   * @param _a a {@link org.eclipse.steady.sign.Signature} object.
   * @param _b a {@link org.eclipse.steady.sign.Signature} object.
   * @return a float.
   */
  public float computeSimilarity(Signature _a, Signature _b);

  /**
   * Compares the signature at hand with the signature provided as argument and computes a so-called signature change, i.e., a set of modifications that transforms one signature into the other one.
   * This method may be applied to the vulnerable and fixed version of all programming constructs touched as part of a security fix.
   *
   * @return the set of modifications required to transform one signature into the other one
   * @param _a a {@link org.eclipse.steady.sign.Signature} object.
   * @param _b a {@link org.eclipse.steady.sign.Signature} object.
   */
  public SignatureChange computeChange(Signature _a, Signature _b);

  /**
   * Returns true if the code modifications expressed by the signature change are contained in the signature at hand, false otherwise.
   * In other words, applied to the Vulas use-case, one could check whether a signature part of an application under analysis is vulnerable or not.
   *
   * @param _change the signature change to be searched for
   * @param _s a {@link org.eclipse.steady.sign.Signature} object.
   * @return a boolean.
   */
  public boolean containsChange(Signature _s, SignatureChange _change);
}
