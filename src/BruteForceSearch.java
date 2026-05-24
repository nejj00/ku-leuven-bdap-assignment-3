
/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.*;

/**
 * Brute force implementation of the similarity searcher. The Jaccard
 * similarity is computed for all pairs and the most similar ones are
 * selected.
 * 
 * @param <T> the type used to represent the shingle set of a document
 */
public class BruteForceSearch<T> extends SimilaritySearcher<T> {

    /**
     * Construct a BruteForceSearch object.
     * 
     * @param reader a data Reader object
     */
    public BruteForceSearch(Reader<T> reader) {
        super(reader);
    }

    /**
     * Get pairs of objects with similarity above threshold.
     * 
     * @param threshold the similarity threshold
     * @return the pairs
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        System.out.println("Reading and shingling documents...");
        List<T> docToShingle = reader.readAll();

        System.out.println("Computing similarities...");
        Set<SimilarPair> cands = new HashSet<SimilarPair>();
        int n = docToShingle.size();

        for (int obj1 = 0; obj1 < n; obj1++) {
            String id1 = reader.getExternalId(obj1);

            for (int obj2 = 0; obj2 < obj1; obj2++) {
                String id2 = reader.getExternalId(obj2);

                double sim = jaccardSimilarity(docToShingle.get(obj1), docToShingle.get(obj2));

                if (sim > threshold) {
                    cands.add(new SimilarPair(id2, id1, sim));
                }
            }
        }
        return cands;
    }
}
