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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.LevenshteinSimilarityCalculator;

public class StringSimilarityLevenshtein {

    private LevenshteinSimilarityCalculator fLevenshtein = new LevenshteinSimilarityCalculator();

    // @Ignore
    @Test
    public void checkWithSimpackConformance() throws Exception {
        String sa1 = new String("Language");
        String sa2 = new String("Languages");
        String sa3 = new String("Levenshtein");
        String sa4 = new String("shteinLeven");
        assertThat(getSimilarity(sa1, sa1), is(1d));
        assertThat(getSimilarity(sa1, sa2), is(1d - (1d / 9d)));
        assertThat(getSimilarity(sa2, sa1), is(1d - (1d / 9d)));
        assertThat(getSimilarity("", sa1), is(1d - (8d / 8d)));
        assertThat(getSimilarity(sa3, sa4), is((11d - 8d) / 11d));
        assertThat(getSimilarity(sa1, sa3), is((2d) / 11d));
    }

    private Double getSimilarity(String sa1, String sa2) {
        return fLevenshtein.calculateSimilarity(sa1, sa2);
    }

    @Test
    public void testLevenshteinSimilarity() {

        String sa1 =
                new String(
                        "HttpRoutedConnection conn ="
                            + " (HttpRoutedConnection)context.getAttribute(\"http.connection\");");
        String sa2 =
                new String(
                        "HttpRoutedConnection conn = (HttpRoutedConnection)"
                            + " context.getAttribute(ExecutionContext.HTTP_CONNECTION);");
        String sa3 = new String("verticalDrawAction");
        String sa4 = new String("drawV erticalAction");
        String sa5 =
                new String(
                        " throw new IllegalArgumentException(\"The buffer size specified for the"
                            + " MultipartStream is too small\");");
        String sa6 =
                new String("throw new IllegalArgumentException(\"boundary may not be null\");");

        System.out.println(fLevenshtein.calculateSimilarity(sa1, sa2));
        System.out.println(fLevenshtein.calculateSimilarity(sa3, sa4));
        System.out.println(fLevenshtein.calculateSimilarity(sa5, sa6));
    }
}
