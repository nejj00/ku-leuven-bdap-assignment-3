
/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */

import java.util.*;
import java.util.function.Consumer;

/**
 * Implementation of minhash and locality sensitive hashing (LSH) to find
 * similar objects.
 * 
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

    public LSH(Reader<T> reader, int numHashes, int numBands, int numBuckets, int seed) {
        super(reader);
        this.numHashes = numHashes;
        this.numBands = numBands;
        this.numBuckets = numBuckets;
        this.seed = seed;
    }

    // THIS METHOD IS REQUIRED
    public Set<SimilarPair> lsh(S signatureMatrix, int numDocs, int numBands, int numBuckets, int seed,
            double threshold) {
        int[][] sigMatrix = (int[][]) signatureMatrix;
        int rowsPerBand = numHashes / numBands;
        Set<SimilarPair> allCandidates = new HashSet<>();

        for (int band = 0; band < numBands; band++) {
            int bandStart = band * rowsPerBand;

            // Build buckets for this band: bucketId -> list of doc indices
            Map<Integer, List<Integer>> curBand = new HashMap<>();

            for (int doc = 0; doc < numDocs; doc++) {
                // Hash the band vector for this document into a bucket
                int[] bandVector = new int[rowsPerBand];

                for (int row = 0; row < rowsPerBand; row++) {
                    bandVector[row] = sigMatrix[bandStart + row][doc];
                }

                int bucketHash = Math.floorMod(Arrays.hashCode(bandVector), numBuckets);

                curBand.computeIfAbsent(bucketHash, k -> new ArrayList<>()).add(doc);
            }

            // Find similar pairs within each bucket
            allCandidates.addAll(
                    getSimilarPairsAboveThresholdForBand(signatureMatrix, threshold, curBand));
        }
        return allCandidates;
    }

    public void lsh_stream(S signatureMatrix, int numDocs, int numBands, int numBuckets, int seed,
            double threshold, Consumer<SimilarPair> sink) {
        int[][] sigMatrix = (int[][]) signatureMatrix;
        int rowsPerBand = numHashes / numBands;
        Set<SimilarPair> seenPairs = new HashSet<>();

        for (int band = 0; band < numBands; band++) {
            int bandStart = band * rowsPerBand;

            // Build buckets for this band: bucketId -> list of doc indices
            Map<Integer, List<Integer>> curBand = new HashMap<>();

            for (int doc = 0; doc < numDocs; doc++) {
                // Hash the band vector for this document into a bucket
                int[] bandVector = new int[rowsPerBand];

                for (int row = 0; row < rowsPerBand; row++) {
                    bandVector[row] = sigMatrix[bandStart + row][doc];
                }

                int bucketHash = Math.floorMod(Arrays.hashCode(bandVector), numBuckets);

                curBand.computeIfAbsent(bucketHash, k -> new ArrayList<>()).add(doc);
            }

            // Find similar pairs within each bucket
            getSimilarPairsAboveThresholdForBand_stream(signatureMatrix, threshold, curBand, sink, seenPairs);
        }
    }

    // THIS METHOD IS REQUIRED
    public Set<SimilarPair> getSimilarPairsAboveThresholdForBand(S signatureMatrix, double threshold,
            Map<Integer, List<Integer>> curBand) {
        Set<SimilarPair> candidates = new HashSet<>();
        int[][] sigMatrix = (int[][]) signatureMatrix;

        // For each bucket, check all pairs of documents that landed in it
        for (List<Integer> bucket : curBand.values()) {
            if (bucket.size() < 2)
                continue;

            // Check all pairs within this bucket
            for (int i = 0; i < bucket.size(); i++) {
                for (int j = i + 1; j < bucket.size(); j++) {
                    int doc1 = bucket.get(i);
                    int doc2 = bucket.get(j);

                    // Estimate similarity using the full signature matrix
                    int matches = 0;
                    for (int row = 0; row < numHashes; row++) {
                        if (sigMatrix[row][doc1] == sigMatrix[row][doc2]) {
                            matches++;
                        }
                    }
                    double sim = (double) matches / numHashes;

                    if (sim >= threshold) {
                        String id1 = reader.getExternalId(doc1);
                        String id2 = reader.getExternalId(doc2);
                        candidates.add(new SimilarPair(id1, id2, sim));
                    }
                }
            }
        }
        return candidates;
    }

    public void getSimilarPairsAboveThresholdForBand_stream(S signatureMatrix, double threshold,
            Map<Integer, List<Integer>> curBand, Consumer<SimilarPair> sink, Set<SimilarPair> seenPairs) {
        int[][] sigMatrix = (int[][]) signatureMatrix;

        // For each bucket, check all pairs of documents that landed in it
        for (List<Integer> bucket : curBand.values()) {
            if (bucket.size() < 2)
                continue;

            // Check all pairs within this bucket
            for (int i = 0; i < bucket.size(); i++) {
                for (int j = i + 1; j < bucket.size(); j++) {
                    int doc1 = bucket.get(i);
                    int doc2 = bucket.get(j);

                    // Estimate similarity using the full signature matrix
                    int matches = 0;
                    for (int row = 0; row < numHashes; row++) {
                        if (sigMatrix[row][doc1] == sigMatrix[row][doc2]) {
                            matches++;
                        }
                    }
                    double sim = (double) matches / numHashes;

                    if (sim >= threshold) {
                        String id1 = reader.getExternalId(doc1);
                        String id2 = reader.getExternalId(doc2);
                        SimilarPair pair = new SimilarPair(id1, id2, sim);

                        if (seenPairs.add(pair)) {
                            sink.accept(pair);
                        }
                    }
                }
            }
        }
    }

    // THIS METHOD IS REQUIRED
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing hash parameters ... ");
        Minhash.HashParameters params = new Minhash.HashParameters(numHashes, reader.getNumShingles(), seed);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Constructing the signature matrix ... ");
        Minhash.MinhashResult<S> result = Minhash.constructSignatureMatrix(reader, params, numHashes);
        this.signatureMatrix = result.signatureMatrix;
        this.numDocs = result.numDocs;
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Creating lsh buckets ... ");
        Set<SimilarPair> similarPairsAboveThreshold = lsh(signatureMatrix, numDocs, numBands, numBuckets, seed,
                threshold);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");

        return similarPairsAboveThreshold;
    }

    @Override
    public void streamSimilarPairsAboveThreshold(double threshold, Consumer<SimilarPair> sink) {
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing hash parameters ... ");
        Minhash.HashParameters params = new Minhash.HashParameters(numHashes, reader.getNumShingles(), seed);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Constructing the signature matrix ... ");
        Minhash.MinhashResult<S> result = Minhash.constructSignatureMatrix(reader, params, numHashes);
        this.signatureMatrix = result.signatureMatrix;
        this.numDocs = result.numDocs;

        double mb_simpler = (4.0 * numHashes * numDocs) / (1024 * 1024);
        System.out.println("Raw data only ~ " + mb_simpler + " MB");

        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");

        startTime = System.currentTimeMillis();
        System.out.println("Creating lsh buckets ... ");
        lsh_stream(signatureMatrix, numDocs, numBands, numBuckets, seed, threshold, sink);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");
    }
}
