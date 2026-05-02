/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
 * copy or distribute without permission. Written by Pieter Robberechts, 2026
 */

/**
 * SimilarPair contains the ids of two objects and their similarity.
 */
public class SimilarPair implements Comparable<SimilarPair>{
	String id1;
	String id2;
	double sim;
	
	/**
	 * Construct a SimilarPair object
	 * @param id1 id of object 1
	 * @param id2 id of object 2
	 * @param sim their similarity
	 */
	public SimilarPair(String id1, String id2, double sim){
		this.id1 = id1;
		this.id2 = id2;
		this.sim = sim;
	}

	/**
	 * Comparing a SimilarPair object to another SimilarPair object.
	 */
	@Override
	public int compareTo(SimilarPair c) {
		if (sim < c.getSimilarity()){
			return -1; 
		}else if (sim == c.getSimilarity()){
			return 0;
		}else{
			return 1;
		}
	}
	
	/**
	 * Returns the id of object 1.
	 */
	public String getId1() {
		return id1;
	}

	/**
	 * Returns the id of object 2.
	 */
	public String getId2() {
		return id2;
	}

	/**
	 * Returns the similarity between the objects.
	 */
	public double getSimilarity(){
		return sim;
	}
	
    @Override
    public boolean equals(Object obj) {
	if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SimilarPair other = (SimilarPair) obj;
        
        if (this.id1 == null || this.id2 == null || other.id1 == null || other.id2 == null) {
            return false;
        }

        // Check for exact match (A, B) == (A, B)
        boolean exactMatch = this.id1.equals(other.id1) && this.id2.equals(other.id2);
        
        // Check for flipped match (A, B) == (B, A)
        boolean flippedMatch = this.id1.equals(other.id2) && this.id2.equals(other.id1);
        
        return exactMatch || flippedMatch;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
        
        // Get standard string hash codes (defaulting to 0 if somehow null)
        int h1 = (id1 == null) ? 0 : id1.hashCode();
        int h2 = (id2 == null) ? 0 : id2.hashCode();
        
        // We add (or XOR) the hash codes to maintain symmetry. 
        // This guarantees that SimilarPair("A", "B") hashes exactly the same as SimilarPair("B", "A").
        return prime * (1 + h1 + h2);
    }

    @Override
    public String toString() {
        return "SimilarPair [id1=" + id1 + ", id2=" + id2 + ", sim=" + sim + "]";
    }
}
