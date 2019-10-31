package com.sap.psr.vulas.java.sign;

import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.NGramsCalculator;

public class StringSimilarityNGrams extends StringSimilarity {

  // bi-grams are used here
  @Override
  protected double calculateSimilarity(String left, String right) {
    return new NGramsCalculator(2).calculateSimilarity(left, right);
  }
}
