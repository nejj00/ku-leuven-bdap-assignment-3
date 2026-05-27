
/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * Class for computing MinHash signatures.
 */
public final class Minhash {

    private Minhash() {
    }

    /**
     * Helper class to hold parameters for universal hashing.
     */
    public static class HashParameters {
        public final int[] a;
        public final int[] b;
        public final int prime;
        public final int numValues;

        // THIS METHOD IS REQUIRED
        public HashParameters(int numHashes, int numValues, int seed) {
            // \begin{stub}
            this.numValues = numValues;
            this.prime = Primes.findLeastPrimeNumber(numValues + 1);

            Random rng = new Random(seed);
            this.a = new int[numHashes];
            this.b = new int[numHashes];

            for (int i = 0; i < numHashes; i++) {
                this.a[i] = rng.nextInt(prime - 1) + 1;
                this.b[i] = rng.nextInt(prime);
            }
            // \end{stub}
        }
    }

    public static class MinhashResult<S> {
        public final S signatureMatrix;
        public final int numDocs;

        public MinhashResult(S signatureMatrix, int numDocs) {
            this.signatureMatrix = signatureMatrix;
            this.numDocs = numDocs;
        }
    }

    /**
     * Construct the signature matrix using on-the-fly hashing.
     * 
     * @param <T>       the type used to represent the shingle set of a document
     * @param <S>       the type used to represent the signature matrix
     * @param reader    iterator returning the representation of objects
     * @param params    the hash function parameters (a, b, prime)
     * @param numHashes number of hashes to use
     * @return a MinhashResult containing the signature matrix and the number of
     *         documents read
     */
    // THIS METHOD IS REQUIRED
    public static <T, S> MinhashResult<S> constructSignatureMatrix(Reader<T> reader, HashParameters params,
            int numHashes) {
        int maxDocs = reader.getMaxDocs();

        int[][] signatureMatrix = new int[numHashes][maxDocs];
        for (int i = 0; i < numHashes; i++) {
            Arrays.fill(signatureMatrix[i], Integer.MAX_VALUE);
        }

        reader.reset();
        int docIndex = 0;
        HashSet<Integer> shingleSet;
        T doc;

        while ((doc = reader.next()) != null) {
            shingleSet = (HashSet<Integer>) doc;

            for (int shingle : shingleSet) {
                for (int hashIndex = 0; hashIndex < numHashes; hashIndex++) {

                    int hashValue = (int) (((long) params.a[hashIndex] * shingle + params.b[hashIndex]) % params.prime)
                            % params.numValues;

                    if (hashValue < signatureMatrix[hashIndex][docIndex]) {
                        signatureMatrix[hashIndex][docIndex] = hashValue;
                    }
                }
            }

            docIndex++;
        }

        // \begin{stub}
        return new MinhashResult<>((S) signatureMatrix, docIndex);
        // return null;
        // \end{stub}
    }
}
