#!/bin/bash

set -e

# ============================================================================
# LSH Full Dataset Runner
# ============================================================================

# Dataset directory
DATAFOLDER="/home/neji/ku-leuven-bdap-assignment-3/data"

# Output file
OUTPUT="output.tsv"

# Experiment parameters
THRESHOLD=0.8
NB_DOCS=1000000
SHINGLE_LENGTH=9
NB_SHINGLES=30000
NB_HASHES=200
NB_BANDS=20
NB_BUCKETS=2147483647

# Java settings
HEAP_SIZE="2g"

# Directories
CLASS_DIR="bin"

echo "============================================================"
echo "Running LSH on full dataset"
echo "============================================================"

time java -cp .:${CLASS_DIR} -Xmx${HEAP_SIZE} Runner \
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

echo "============================================================"
echo "Finished"
echo "============================================================"