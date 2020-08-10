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
package com.sap.psr.vulas.shared.enums;

/**
 * The client running goal(s).
 */
public enum GoalClient {
    CLI((byte) 10),
    MAVEN_PLUGIN((byte) 20),
    REST_SERVICE((byte) 30),
    AGENT((byte) 40),
    SETUPTOOLS((byte) 50),
    GRADLE_PLUGIN((byte) 60);

    private byte value;

    private GoalClient(byte _value) {
        this.value = _value;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        if (this.value == 10) return "CLI";
        else if (this.value == 20) return "MAVEN";
        else if (this.value == 30) return "REST";
        else if (this.value == 40) return "AGENT";
        else if (this.value == 50) return "SETUPTOOLS";
        else if (this.value == 60) return "GRADLE";
        else throw new IllegalArgumentException("[" + this.value + "] is not a valid goal client");
    }
}
