/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a set of documents and constructs shingle representations for
 * these documents.
 * @param <T> the type used to represent the shingle set of a document
 */
public abstract class Reader<T> {

    public Shingler<T> shingler;
    protected int maxDocs;
    public List<String> idToDoc = new ArrayList<String>();
    protected int curDoc;

    public Reader(int maxDocs, Shingler<T> shingler) {
        this.maxDocs = maxDocs;
        this.shingler = shingler;
        this.curDoc = -1;
    }

    abstract public T next();
    abstract public void reset();

    public boolean hasNext() {
        if (this.maxDocs < 0) return true;
        return this.curDoc < this.maxDocs - 1;
    };

    public List<T> readAll() {
        reset();
        List<T> idToShingle = new ArrayList<T>();
        while (this.hasNext()){
            T s = this.next();
            if (s != null) {
                idToShingle.add(s);
            }
        }
        System.out.println("Read " + idToShingle.size() + " documents into memory.");
        return idToShingle;
    }

    public int getNumShingles() {
        return this.shingler.getNumShingles();
    }

    public int getMaxDocs() {
        return this.maxDocs;
    }

    public String getExternalId(int id) {
        return this.idToDoc.get(id);
    }
}
