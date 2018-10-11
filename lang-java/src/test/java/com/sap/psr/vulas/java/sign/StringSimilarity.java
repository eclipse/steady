package com.sap.psr.vulas.java.sign;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public abstract class StringSimilarity {

    protected abstract double calculateSimilarity(String left, String right);

    @Test
    public void emptyStringsShouldBeSimilar() throws Exception {
        assertThat(calculateSimilarity("", ""), is(1.0));
    }

    @Test
    public void identicalStringsShouldBeSimilar() throws Exception {
        assertThat(calculateSimilarity("change distiller", "change distiller"), is(1.0));
    }

}
