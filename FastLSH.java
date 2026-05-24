
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
public class FastLSH<T, S> extends SimilaritySearcher<T> {

    int numHashes;
    int numBands;
    int numBuckets;
    int seed;
    S signatureMatrix = null;
    int numDocs;

    // private List<T> documents;

    long candidatePairs = 0;
    long duplicatePairs = 0;
    long emittedPairs = 0;

    long totalBuckets = 0;
    long nonEmptyBuckets = 0;
    long singletonBuckets = 0;
    long maxBucketSize = 0;
    long totalBucketEntries = 0;

    public FastLSH(Reader<T> reader, int numHashes, int numBands, int numBuckets, int seed) {
        super(reader);
        this.numHashes = numHashes;
        this.numBands = numBands;
        this.numBuckets = numBuckets;
        this.seed = seed;
    }

    public void lsh_stream(S signatureMatrix, int numDocs, int numBands, int numBuckets, int seed,
            double threshold, Consumer<SimilarPair> sink) {
        int[][] sigMatrix = (int[][]) signatureMatrix;
        int rowsPerBand = numHashes / numBands;
        Set<Long> seenPairs = new HashSet<>();

        for (int band = 0; band < numBands; band++) {
            int bandStart = band * rowsPerBand;

            // Build buckets for this band: bucketId -> list of doc indices
            Map<Integer, List<Integer>> curBand = new HashMap<>();

            for (int doc = 0; doc < numDocs; doc++) {
                // Hash the band vector for this document into a bucket
                // int[] bandVector = new int[rowsPerBand];

                // for (int row = 0; row < rowsPerBand; row++) {
                // bandVector[row] = sigMatrix[bandStart + row][doc];
                // }

                // int bucketHash = Math.floorMod(Arrays.hashCode(bandVector), numBuckets);

                int hash = 1;

                for (int row = 0; row < rowsPerBand; row++) {
                    hash = 31 * hash + sigMatrix[bandStart + row][doc];
                }

                int bucketHash = Math.floorMod(hash, numBuckets);
                // int bucketHash = Math.floorMod(hash, numBuckets);

                curBand.computeIfAbsent(bucketHash, k -> new ArrayList<>()).add(doc);
            }

            // Find similar pairs within each bucket
            getSimilarPairsAboveThresholdForBand_stream(signatureMatrix, threshold, curBand, sink, seenPairs);
        }

        long estimatedBytes = seenPairs.size() * Long.BYTES;
        double estimatedMB = estimatedBytes / (1024.0 * 1024.0);

        System.out.printf("Estimated seenPairs memory: %.2f MB%n", estimatedMB);

    }

    public void getSimilarPairsAboveThresholdForBand_stream(S signatureMatrix, double threshold,
            Map<Integer, List<Integer>> curBand, Consumer<SimilarPair> sink, Set<Long> seenPairs) {
        // For each bucket, check all pairs of documents that landed in it
        int[][] sigMatrix = (int[][]) signatureMatrix;

        for (List<Integer> bucket : curBand.values()) {
            if (bucket.size() < 2)
                continue;

            totalBuckets++;

            int size = bucket.size();

            totalBucketEntries += size;

            if (size > 0)
                nonEmptyBuckets++;
            if (size == 1)
                singletonBuckets++;

            maxBucketSize = Math.max(maxBucketSize, size);

            // Check all pairs within this bucket
            for (int i = 0; i < bucket.size(); i++) {
                for (int j = i + 1; j < bucket.size(); j++) {
                    // int doc1 = bucket.get(i);
                    // int doc2 = bucket.get(j);

                    candidatePairs++;

                    if (!seenPairs.add(pairKey(bucket.get(i), bucket.get(j)))) {
                        duplicatePairs++;
                        continue;
                    }

                    // HashSet<Integer> s1 = (HashSet<Integer>) documents.get(doc1);
                    // HashSet<Integer> s2 = (HashSet<Integer>) documents.get(doc2);

                    // if (!canReachThreshold(s1, s2, threshold)) {
                    // continue;
                    // }

                    // double sim = jaccardSimilarity(documents.get(doc1), documents.get(doc2));

                    // Estimate similarity using the full signature matrix
                    int matches = 0;
                    for (int row = 0; row < numHashes; row++) {
                        if (sigMatrix[row][bucket.get(i)] == sigMatrix[row][bucket.get(j)]) {
                            matches++;
                        }
                    }
                    double sim = (double) matches / numHashes;

                    if (sim >= threshold) {
                        emittedPairs++;
                        sink.accept(new SimilarPair(
                                reader.getExternalId(bucket.get(i)),
                                reader.getExternalId(bucket.get(j)),
                                sim));
                    }
                }
            }
        }
    }

    private boolean canReachThreshold(
            HashSet<Integer> s1,
            HashSet<Integer> s2,
            double threshold) {

        int min = Math.min(s1.size(), s2.size());
        int max = Math.max(s1.size(), s2.size());

        return ((double) min / max) >= threshold;
    }

    private long pairKey(int a, int b) {
        int x = Math.min(a, b);
        int y = Math.max(a, b);
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    @Override
    public void streamSimilarPairsAboveThreshold(double threshold, Consumer<SimilarPair> sink) {

        printMemory("before signature initialization"); 
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing hash parameters ... ");
        Minhash.HashParameters params = new Minhash.HashParameters(numHashes, reader.getNumShingles(), seed);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");
        printMemory("after signature initialization");

        printMemory("before signature matrix construction");
        startTime = System.currentTimeMillis();
        System.out.println("Constructing the signature matrix ... ");
        Minhash.MinhashResult<S> result = Minhash.constructSignatureMatrix(reader,
                params, numHashes);
            
        // System.out.println("Reading documents into memory ... ");
        // this.documents = reader.readAll();

        // System.out.println("Constructing the signature matrix ... ");
        // Minhash.MinhashResult<S> result =
        // Minhash.constructSignatureMatrixFromList(documents, params, numHashes);

        this.signatureMatrix = result.signatureMatrix;
        this.numDocs = result.numDocs;

        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");
        printMemory("after signature matrix construction");

        printMemory("before lsh stream");
        startTime = System.currentTimeMillis();
        System.out.println("Creating lsh buckets ... ");
        lsh_stream(signatureMatrix, numDocs, numBands, numBuckets, seed, threshold, sink);
        printMemory("after lsh stream");

        System.out.println("===== LSH STATS =====");
        System.out.println("Candidate pairs: " + candidatePairs);
        System.out.println("Duplicate candidates: " + duplicatePairs);
        System.out.println("Output pairs: " + emittedPairs);

        System.out.println("Total buckets: " + totalBuckets);
        System.out.println("Singleton buckets: " + singletonBuckets);

        System.out.println("Max bucket size: " + maxBucketSize);

        double avgBucket = totalBuckets == 0
                ? 0
                : ((double) totalBucketEntries / totalBuckets);

        System.out.println("Average bucket size: " + avgBucket);
        System.out.println("done! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
        System.out.println("--------------");
    }

    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold(double threshold) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSimilarPairsAboveThreshold'");
    }

    private static void printMemory(String label) {
        Runtime rt = Runtime.getRuntime();

        long used = rt.totalMemory() - rt.freeMemory();
        double usedMB = used / (1024.0 * 1024.0);

        System.out.printf("[MEM] %s: %.2f MB%n", label, usedMB);
    }
}
