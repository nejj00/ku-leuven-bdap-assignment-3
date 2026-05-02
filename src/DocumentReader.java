/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Reads web documents from a list of JSONL files (compressed or uncompressed) 
 * and constructs shingle representations for these documents.
 * @param <T> the type used to represent the shingle set of a document
 */
public class DocumentReader<T> extends Reader<T> {

    private List<File> files;
    private int currentFileIndex;
    private BufferedReader br;

    public DocumentReader(int maxDocs, Shingler<T> shingler, List<File> files) {
        super(maxDocs, shingler);
        this.files = files;
        reset();
    }

    @Override
    public T next() {
        try {
            while (true) {
                // Check if we hit the limit of valid documents to read
                if (this.maxDocs >= 0 && this.idToDoc.size() >= this.maxDocs) {
                    return null;
                }

                String line = null;
                // Keep trying to read a line from the current or next files
                while (true) {
                    if (this.br != null) {
                        line = this.br.readLine();
                        if (line != null) break; 
                    }
                    if (!advanceToNextFile()) break; 
                }

                // True EOF across all files
                if (line == null) return null; 

                this.curDoc++; // Keep track of total lines attempted
                if (this.curDoc % 100000 == 0 && this.curDoc > 0) {
                    System.out.println("Read " + this.curDoc + " lines, " + this.idToDoc.size() + " valid docs...");
                }

                String text = extractTextFromJson(line);
                T shingles = this.shingler.shingle(text);

                // Only accept documents that passed the filter (shingles != null)
                if (shingles != null) {
                    String url = extractUrlFromJson(line);
                    this.idToDoc.add(url);
                    return shingles;
                }
                
                // If shingles == null, the loop continues to the next line automatically
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean advanceToNextFile() throws IOException {
        if (this.br != null) {
            this.br.close();
            this.br = null;
        }
        
        this.currentFileIndex++;
        if (this.files != null && this.currentFileIndex < this.files.size()) {
            File nextFile = this.files.get(this.currentFileIndex);
            FileInputStream fis = new FileInputStream(nextFile);
            int bufferSize = 128 * 1024;
            if (nextFile.getName().endsWith(".gz")) {
                GZIPInputStream gis = new GZIPInputStream(fis, 64 * 1024);
                this.br = new BufferedReader(new InputStreamReader(gis, "UTF-8"), bufferSize);
            } else {
                this.br = new BufferedReader(new InputStreamReader(fis, "UTF-8"), bufferSize);
            }
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        try {
            this.currentFileIndex = -1;
            this.curDoc = -1;
            this.idToDoc.clear();
            advanceToNextFile();
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        // hasNext() now refers to whether we've reached the limit of valid docs
        if (this.maxDocs < 0) return true;
        return this.idToDoc.size() < this.maxDocs;
    }

    private String extractUrlFromJson(String jsonLine) {
        String urlMarker = "\"url\":";
        int markerIndex = jsonLine.indexOf(urlMarker);
        if (markerIndex == -1) return "doc_" + this.idToDoc.size();
        int startIndex = jsonLine.indexOf("\"", markerIndex + urlMarker.length());
        if (startIndex == -1) return "doc_" + this.idToDoc.size();
        startIndex++;
        int endIndex = jsonLine.indexOf("\"", startIndex);
        if (endIndex == -1) return "doc_" + this.idToDoc.size();
        return jsonLine.substring(startIndex, endIndex);
    }

    private String extractTextFromJson(String jsonLine) {
        String textMarker = "\"text\":";
        int markerIndex = jsonLine.indexOf(textMarker);
        if (markerIndex == -1) return "";
        int startIndex = jsonLine.indexOf("\"", markerIndex + textMarker.length());
        if (startIndex == -1) return "";
        startIndex++;
        int endIndex = startIndex;
        int lineLen = jsonLine.length();
        while (endIndex < lineLen) {
            char c = jsonLine.charAt(endIndex);
            if (c == '"' && jsonLine.charAt(endIndex - 1) != '\\') break;
            endIndex++;
        }
        if (endIndex >= lineLen) return "";
        String rawText = jsonLine.substring(startIndex, endIndex);
        return rawText.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
