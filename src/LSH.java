/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */

import java.util.*;
import java.io.*;

/**
 * Implementation of minhash and locality sensitive hashing (LSH) to find
 * similar objects.
 * @param <T> the type used to represent the shingle set of a document
 * @param <S> the type used to represent the signature matrix
 */
public class LSH<T, S> extends SimilaritySearcher<T> {

    int numHashes;
    int numBands;
    int numBuckets;
    int seed;
    S signatureMatrix = null;
    int numDocs;

    public LSH(Reader<T> reader, int numHashes, int numBands, int numBuckets, int seed){
        super(reader);
        this.numHashes = numHashes;
        this.numBands = numBands;
        this.numBuckets = numBuckets;
        this.seed = seed;
    }

    // THIS METHOD IS REQUIRED
    public Set<SimilarPair> lsh(S signatureMatrix, int numDocs, int numBands, int numBuckets, int seed, double threshold) {
        /*
        //\begin{stub}
        return null;
        //\end{stub}
        */
    }

    // THIS METHOD IS REQUIRED
    public Set<SimilarPair> getSimilarPairsAboveThresholdForBand(S signatureMatrix, double threshold, Map<Integer, List<Integer>> curBand){
        /*
        //\begin{stub}
        return null;
        //\end{stub}
        */
    }

    // THIS METHOD IS REQUIRED
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing hash parameters ... ");
        Minhash.HashParameters params = new Minhash.HashParameters(numHashes, reader.getNumShingles(), seed);
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Constructing the signature matrix ... ");
        Minhash.MinhashResult<S> result = Minhash.constructSignatureMatrix(reader, params, numHashes);
        this.signatureMatrix = result.signatureMatrix;
        this.numDocs = result.numDocs;
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Creating lsh buckets ... ");
        Set<SimilarPair> similarPairsAboveThreshold = lsh(signatureMatrix, numDocs, numBands, numBuckets, seed, threshold);
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");

        return similarPairsAboveThreshold;
    }
}
