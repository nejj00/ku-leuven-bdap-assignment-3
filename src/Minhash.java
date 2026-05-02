/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.Random;
import java.util.Arrays;

/**
 * Class for computing MinHash signatures.
 */
public final class Minhash {

    private Minhash(){
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
            /*
            //\begin{stub}
            // TODO: Initialize parameters for universal hashing
            this.numValues = numValues;
            this.prime = 0;
            this.a = null;
            this.b = null;
            //\end{stub}
            */
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
     * @param <T> the type used to represent the shingle set of a document
     * @param <S> the type used to represent the signature matrix
     * @param reader iterator returning the representation of objects
     * @param params the hash function parameters (a, b, prime)
     * @param numHashes number of hashes to use
     * @return a MinhashResult containing the signature matrix and the number of documents read
     */
    // THIS METHOD IS REQUIRED
    public static <T, S> MinhashResult<S> constructSignatureMatrix(Reader<T> reader, HashParameters params, int numHashes) {
        /*
        //\begin{stub}
        return null; 
        //\end{stub}
        */
    }
}
