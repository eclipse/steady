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
package org.eclipse.steady.shared.cache;

/**
 * Used to fetch objects from a remote backing store.
 *
 * @param <S>
 * @param <T>
 */
public interface ObjectFetcher<S, T> {

  /**
   * This method is called by {@link Cache#get(Object)} in case of a cache miss for the given key.
   * The method must be implemented in order to fetch the object with the given key from the (remote) backing store.
   *
   * @param _key a S object.
   * @throws org.eclipse.steady.shared.cache.CacheException
   * @return a T object.
   */
  public T fetch(S _key) throws CacheException;
}
