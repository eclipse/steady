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
package com.sap.psr.vulas.sign;

import java.io.File;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * <p>SignatureFactory interface.</p>
 *
 */
public interface SignatureFactory {
	
	/**
	 * Returns true if the factory can compute a signature for the given {@link ConstructId}, false otherwise.
	 *
	 * @param _id a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return a boolean.
	 */
	public boolean isSupportedConstructId(ConstructId _id);

	/**
	 * Creates a {@link Signature} of the body of the given {@link Construct}.
	 *
	 * @param _construct a {@link com.sap.psr.vulas.Construct} object.
	 * @return a {@link com.sap.psr.vulas.sign.Signature} object.
	 */
	public Signature createSignature(Construct _construct);
	
	/**
	 * Creates a {@link Signature} of the given {@link ConstructId} on the basis of the given file.
	 *
	 * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @param _file a {@link java.io.File} object.
	 * @return a {@link com.sap.psr.vulas.sign.Signature} object.
	 */
	public Signature createSignature(ConstructId _cid, File _file);
	
	/**
	 * <p>computeChange.</p>
	 *
	 * @param _from a {@link com.sap.psr.vulas.Construct} object.
	 * @param _to a {@link com.sap.psr.vulas.Construct} object.
	 * @return a {@link com.sap.psr.vulas.sign.SignatureChange} object.
	 */
	public SignatureChange computeChange(Construct _from, Construct _to);
}
