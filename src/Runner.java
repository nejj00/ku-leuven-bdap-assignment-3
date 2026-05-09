/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.io.*;
import java.util.*;

/**
 * The Runner can be ran from the commandline to find the most similar pairs
 * of documents in a directory.
 */
public class Runner {

    public static void main(String[] args) {

        String inputFile = "";
        String outputFile = "";
        String method = "";
        int numShingles = 1000;
        int numHashes = -1;
        int numBands = -1;
        int numBuckets = 2000;
        int seed = 1234;
        int maxDocs = -1;
        int shingleLength = -1;
        float threshold = -1f;

        // Parse command line arguments
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i];
            if (arg.equals("-method")) {
                method = args[i+1];
            } else if(arg.equals("-numHashes")) {
                numHashes = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-numBands")) {
                numBands = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-numBuckets")) {
                numBuckets = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-numShingles")) {
                numShingles = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-seed")) {
                seed = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-dataFile")) {
                inputFile = args[i + 1];
            } else if(arg.equals("-maxDocs")) {
                maxDocs = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-shingleLength")) {
                shingleLength = Integer.parseInt(args[i+1]);
            } else if(arg.equals("-threshold")) {
                threshold = Float.parseFloat(args[i+1]);
            } else if(arg.equals("-outputFile")) {
                outputFile = args[i + 1];
            }
            i += 2;
        }

        File dataPath = new File(inputFile);
        List<File> filesToProcess = new ArrayList<>();
        if (dataPath.isDirectory()) {
            File[] files = dataPath.listFiles((dir, name) -> 
                name.endsWith(".gz") || name.endsWith(".jsonl") || name.endsWith(".jsonl.gz"));
            if (files != null) {
                Arrays.sort(files);
                filesToProcess.addAll(Arrays.asList(files));
            }
        } else {
            filesToProcess.add(dataPath);
        }

        //\begin{stub}
        // TODO: Replace the generic types below with your chosen data structures.
        // T: Type for a single document's shingle set (e.g., HashSet<Integer>)
        // S: Type for the entire signature matrix (e.g., int[][])
        Shingler<HashSet<Integer>> shingler = new Shingler<HashSet<Integer>>(shingleLength, numShingles, seed);
        Reader<HashSet<Integer>> reader = new DocumentReader<HashSet<Integer>>(maxDocs, shingler, filesToProcess);
        SimilaritySearcher<HashSet<Integer>> searcher = null;
        //\end{stub}

        if (method.equals("bf")) {
            //\begin{stub}
            searcher = new BruteForceSearch<HashSet<Integer>>(reader);
            //\end{stub}
        } else if(method.equals("lsh")) {
            if (numHashes == -1 || numBands == -1) {
                throw new Error("Both -numHashes and -numBands are mandatory arguments for the LSH method");
            }
            //\begin{stub}
            searcher = new LSH<HashSet<Integer>, int[][]>(reader, numHashes, numBands, numBuckets, seed);
            //\end{stub}
        }

        long startTime = System.currentTimeMillis();
        System.out.println("Searching items more similar than " + threshold + " ... ");
        Set<SimilarPair> similarItems = searcher.getSimilarPairsAboveThreshold(threshold);
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");
        printPairs(similarItems, outputFile);
    }

    public static void printPairs(Set<SimilarPair> similarItems, String outputFile){
        try {
            File fout = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            List<SimilarPair> sim = new ArrayList<>(similarItems);
            Collections.sort(sim, Collections.reverseOrder());
            for(SimilarPair p : sim) {
                bw.write(p.getId1() + "\t" + p.getId2() + "\t" + p.getSimilarity());
                bw.newLine();
            }
            bw.close();
            System.out.println("Found " + similarItems.size() + " similar pairs, saved to '" + outputFile + "'");
            System.out.println("--------------");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
