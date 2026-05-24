##
## Makefile
##
## Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not
## copy or distribute without permission. Written by Pieter Robberechts, 2026
## 

.PHONY: clean bf_small lsh_small lsh_full

# Experiment parameters ######################################################

# Dataset directory
# DATAFOLDER=/cw/bdap/assignment3/
DATAFOLDER=/home/neji/ku-leuven-bdap-assignment-3/data

# Ouptut directory
OUTPUT=output.tsv

# Experiment parameters
THRESHOLD=0.8
# NB_DOCS=8870959
NB_DOCS=1000000
SHINGLE_LENGTH=9
# NB_SHINGLES=1000000
NB_SHINGLES=30000
NB_HASHES=200
NB_BANDS=20
# NB_BUCKETS=1000000
NB_BUCKETS=2147483647
NB_DOCS_SMALL = 15000

NB_DOCS_SMALL_K := $(shell echo $$(($(NB_DOCS_SMALL)/1000)))
NB_SHINGLES_K := $(shell echo $$(($(NB_SHINGLES)/1000)))

# Output file
OUTPUT_BF=output_bf_$(NB_DOCS_SMALL_K)kDocs_$(NB_SHINGLES_K)kShingles.tsv
OUTPUT_LSH=output_lsh_$(NB_DOCS_SMALL_K)kDocs_$(NB_SHINGLES_K)kShingles_$(NB_HASHES)Hashes_$(NB_BANDS)Bands_$(NB_BUCKETS)Buckets.tsv

# Compilation  ###############################################################

## Locate directories
class_d=bin
source_d=src

# Compilation stuff
JAVAC=javac
JFLAGS=-g -d $(class_d) -cp $(class_d) -Xlint:all

all: $(class_d)/Runner.class

clean:
	rm -rf $(class_d)/*

$(class_d)/MurmurHash.class: $(source_d)/MurmurHash.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Primes.class: $(source_d)/Primes.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/SimilarPair.class: $(source_d)/SimilarPair.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Shingler.class: $(source_d)/Shingler.java $(class_d)/MurmurHash.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Reader.class: $(source_d)/Reader.java $(class_d)/Shingler.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/DocumentReader.class: $(source_d)/DocumentReader.java $(class_d)/Reader.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/SimilaritySearcher.class: $(source_d)/SimilaritySearcher.java $(class_d)/Reader.class $(class_d)/SimilarPair.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/BruteForceSearch.class: $(source_d)/BruteForceSearch.java $(class_d)/SimilaritySearcher.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Minhash.class: $(source_d)/Minhash.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/LSH.class: $(source_d)/LSH.java $(class_d)/SimilaritySearcher.class $(class_d)/Primes.class $(class_d)/Minhash.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Runner.class: $(source_d)/Runner.java $(class_d)/DocumentReader.class $(class_d)/BruteForceSearch.class $(class_d)/LSH.class
	@$(JAVAC) $(JFLAGS) $<

# Experiments ################################################################

bf_small: $(class_d)/Runner.class
	@echo "Testing BF on subset of data"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method bf \
		-maxDocs ${NB_DOCS_SMALL} \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT_BF} \
		-threshold ${THRESHOLD} \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES}

lsh_small: $(class_d)/Runner.class
	@echo "Testing LSH on subset of data"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method lsh \
		-maxDocs ${NB_DOCS_SMALL} \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT_LSH} \
		-threshold ${THRESHOLD} \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES} \
		-numHashes ${NB_HASHES} \
		-numBands ${NB_BANDS} \
		-numBuckets ${NB_BUCKETS}

lsh_full: $(class_d)/Runner.class
	@echo "Running LSH on full dataset"
	time java -cp .:$(class_d) -Xmx2g Runner \
		-method lsh \
		-maxDocs ${NB_DOCS} \
		-dataFile ${DATAFOLDER} \
		-outputFile ${OUTPUT} \
		-threshold ${THRESHOLD} \
		-shingleLength ${SHINGLE_LENGTH} \
		-numShingles ${NB_SHINGLES} \
		-numHashes ${NB_HASHES} \
		-numBands ${NB_BANDS} \
		-numBuckets ${NB_BUCKETS}
