#!/bin/bash

set -e

DATA=/home/neji/ku-leuven-bdap-assignment-3/data
OUTDIR=results
mkdir -p $OUTDIR

THRESHOLD=0.8
SHINGLE=9
SHINGLES=1000000
DOCS_SMALL=5000
DOCS_MED=50000

HASHES=(50 100 150)
BANDS=(5 10 20)

echo "doc_size,method,h,b,docs,time,candidates,outputs" > $OUTDIR/results.csv

run() {
  METHOD=$1
  DOCS=$2
  H=$3
  B=$4

  OUT=$OUTDIR/${METHOD}_${DOCS}_${H}_${B}.tsv

  /usr/bin/time -f "%e" -o tmp_time.txt java -cp .:bin -Xmx2g Runner \
    -method $METHOD \
    -maxDocs $DOCS \
    -dataFile $DATA \
    -outputFile $OUT \
    -threshold $THRESHOLD \
    -shingleLength $SHINGLE \
    -numShingles $SHINGLES \
    -numHashes $H \
    -numBands $B \
    -numBuckets 1000000

  TIME=$(cat tmp_time.txt)

  echo "$DOCS,$METHOD,$H,$B,$TIME,NA,NA" >> $OUTDIR/results.csv
}

# ---- baseline BF (small only) ----
run bf $DOCS_SMALL 0 0

# ---- LSH experiments ----
for H in "${HASHES[@]}"; do
  for B in "${BANDS[@]}"; do
    if [ $((H % B)) -eq 0 ]; then
      run lsh $DOCS_SMALL $H $B
      run lsh $DOCS_MED $H $B
    fi
  done
done

echo "Done. Results in $OUTDIR/results.csv"