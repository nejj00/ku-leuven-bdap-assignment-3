/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Searching similar objects. Objects should be represented as a mapping from
 * an object identifier to a set containing the associated values.
 * @param <T> the type used to represent the shingle set of a document
 */
public abstract class SimilaritySearcher<T> {

    Reader<T> reader;

    public SimilaritySearcher(Reader<T> reader) {
        this.reader = reader;
    }

    /**
     * Returns the pairs of the objectMapping that have a similarity coefficient exceeding threshold
     * @param threshold the similarity threshold
     * @return the pairs with similarity above the threshold
     */
    // THIS METHOD IS REQUIRED
    abstract public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold);

    /**
     * Jaccard similarity between two sets of type T.
     * @param set1
     * @param set2
     * @return the similarity
     */
    // THIS METHOD IS REQUIRED
    public double jaccardSimilarity(T set1, T set2) {
        /*
        //\begin{stub}
        // TODO: Implement Jaccard similarity for your chosen type T
        return 0;
        //\end{stub}
        */
    }

}
