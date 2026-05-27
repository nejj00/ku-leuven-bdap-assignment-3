#!/bin/bash

set -e

# ============================================================================
# LSH Full Dataset Runner
# ============================================================================

# Default values
# DEFAULT_DATAFOLDER="/home/neji/ku-leuven-bdap-assignment-3/data"
DEFAULT_DATAFOLDER="/cw/bdap/assignment3/"
DEFAULT_OUTPUT="output.tsv"

# Parse command line arguments
DATAFOLDER="${1:-$DEFAULT_DATAFOLDER}"
OUTPUT="${2:-$DEFAULT_OUTPUT}"

# Experiment parameters
THRESHOLD=0.8
NB_DOCS=1000000
SHINGLE_LENGTH=9
NB_SHINGLES=30000
NB_HASHES=200
NB_BANDS=20
NB_BUCKETS=1000000

echo "============================================================"
echo "Running LSH on full dataset"
echo "============================================================"

echo "Input folder : ${DATAFOLDER}"
echo "Output file  : ${OUTPUT}"

java Runner \
    -method lsh \
    -dataFile ${DATAFOLDER} \
    -outputFile ${OUTPUT} \
    -threshold ${THRESHOLD} \
    -maxDocs ${NB_DOCS} \
    -shingleLength ${SHINGLE_LENGTH} \
    -numShingles ${NB_SHINGLES} \
    -numHashes ${NB_HASHES} \
    -numBands ${NB_BANDS} \
    -numBuckets ${NB_BUCKETS}

echo "============================================================"
echo "Finished"
echo "============================================================"