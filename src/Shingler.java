/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Shingler constructs the word-level shingle representations of documents.
 * @param <T> the type used to represent the shingle set of a document
 */
public class Shingler<T> {

    private int k;
    private int numShingles;
    private int seed;
    private static final int MIN_PARAGRAPH_WORDS = 5;

    public Shingler(int k, int numShingles, int seed) {
        this.k = k;
        this.numShingles = numShingles;
        this.seed = seed;
    }

    /**
     * Get the word-level shingle set representation of a document.
     * 
     * @param doc document that should be shingled
     * @return the shingle representation of the document of type T
     */
    // THIS METHOD IS REQUIRED
    public T shingle(String doc) {
        if (doc == null || doc.isEmpty()) return null;

        // 1. Fast split into words while respecting paragraph filtering
        List<String> validWords = new ArrayList<>(2000);
        int start = 0;
        int len = doc.length();
        int wordsInPara = 0;
        int paraStartIdx = 0;

        for (int i = 0; i <= len; i++) {
            char c = (i == len) ? '\n' : doc.charAt(i);
            if (c == '\n' || Character.isWhitespace(c)) {
                if (i > start) {
                    String word = clean(doc.substring(start, i));
                    if (!word.isEmpty()) {
                        validWords.add(word);
                        wordsInPara++;
                    }
                }
                start = i + 1;
                
                if (c == '\n') {
                    if (wordsInPara < MIN_PARAGRAPH_WORDS) {
                        // Remove words from the short paragraph
                        while (wordsInPara > 0) {
                            validWords.remove(validWords.size() - 1);
                            wordsInPara--;
                        }
                    }
                    wordsInPara = 0;
                }
            }
        }

        if (validWords.isEmpty()) return null;

        // 2. Pre-hash each word
        int numValidWords = validWords.size();
        int[] wordHashes = new int[numValidWords];
        for (int i = 0; i < numValidWords; i++) {
            wordHashes[i] = MurmurHash.hash32(validWords.get(i), this.seed);
        }

        // 3. Construct shingle hashes by combining word hashes (No String Allocations!)
        int numPossibleShingles = Math.max(1, numValidWords - k + 1);
        int[] shingleHashes = new int[numPossibleShingles];

        if (numValidWords < k) {
            int h = seed;
            for (int wh : wordHashes) h = 31 * h + wh;
            shingleHashes[0] = (h & Integer.MAX_VALUE) % numShingles;
        } else {
            for (int i = 0; i < numPossibleShingles; i++) {
                int h = seed;
                for (int j = 0; j < k; j++) {
                    h = 31 * h + wordHashes[i + j];
                }
                shingleHashes[i] = (h & Integer.MAX_VALUE) % numShingles;
            }
        }

        /*
        //\begin{stub}
        // TODO: Return the shingles in your chosen data structure T.
        return null;
        //\end{stub}
        */
    }

    private String clean(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public int getNumShingles() {
        return this.numShingles;
    }
}
