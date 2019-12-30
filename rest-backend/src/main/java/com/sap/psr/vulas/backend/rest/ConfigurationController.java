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
package com.sap.psr.vulas.backend.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.psr.vulas.shared.json.model.KeyValue;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>ConfigurationController class.</p>
 *
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/configuration")
public class ConfigurationController {
	
	private static Logger log = LoggerFactory.getLogger(ConfigurationController.class);
		
	/**
	 * Returns an array of {@link KeyValue}s with configuration settings read from {@link VulasConfiguration}.
	 *
	 * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist, 200 {@link HttpStatus#OK} if the library is found
	 * @param subset a {@link java.lang.String} object.
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<KeyValue[]> getConfiguration(@RequestParam(value="subset", required=false, defaultValue="")String subset) {
		try {
			KeyValue[] values = null;
			if(subset==null || subset.equals(""))
				values = KeyValue.toKeyValue(VulasConfiguration.getGlobal().getConfiguration());
			else
				values = KeyValue.toKeyValue(VulasConfiguration.getGlobal().getConfiguration().subset(subset));
			return new ResponseEntity<KeyValue[]>(values, HttpStatus.OK);
		}
		catch(Exception enfe) {
			return new ResponseEntity<KeyValue[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
