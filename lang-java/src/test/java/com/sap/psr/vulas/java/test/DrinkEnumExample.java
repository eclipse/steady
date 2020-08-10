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
package com.sap.psr.vulas.java.test;

public final class DrinkEnumExample {

    public interface DrinkTypeInterface {

        String getDisplayableType();
    }

    public static enum DrinkType implements DrinkTypeInterface {
        COFFEE("Coffee"),
        TEA("Tea");
        private final String type;

        private DrinkType(final String type) {
            this.type = type;
        }

        public String getDisplayableType() {
            return type;
        }
    }

    public static enum Drink implements DrinkTypeInterface {
        COLUMBIAN("Columbian Blend", DrinkType.COFFEE),
        ETHIOPIAN("Ethiopian Blend", DrinkType.COFFEE),
        MINT_TEA("Mint", DrinkType.TEA),
        HERBAL_TEA("Herbal", DrinkType.TEA),
        EARL_GREY("Earl Grey", DrinkType.TEA);
        private final String label;
        private final DrinkType type;

        private Drink(String label, DrinkType type) {
            this.label = label;
            this.type = type;
        }

        public String getDisplayableType() {
            return label;
        }
    }
}
