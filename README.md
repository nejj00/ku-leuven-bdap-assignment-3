# ku-leuven-bdap-assignment-3

## Dataset Analysis

`'.text | gsub("\n"; " ")'` replaces newline characters with a space.

```bash
# Print first 3 documents from file
zcat c4_shard_01.jsonl.gz | head -n 3 | jq

# Print word count per document (only first 3)
zcat c4_shard_01.jsonl.gz | head -n 3 | jq -r '.text | gsub("\n"; " ")' | awk '{ print NF }'

# Print average word count for entire dataset
zcat c4_shard_01.jsonl.gz | head -n 1000 | jq -r '.text | gsub("\n"; " ")' | awk '{ total += NF; count++ } END { print total/count }'

# Print character count per document, including spaces
zcat c4_shard_01.jsonl.gz | head -n 3 | jq -r '.text | gsub("\n"; " ")' | awk '{ print length }'
```

## Desing decisions made w.r.t runtime and memory efficiency

I decided to stream the output so that I can improve memory usage and not hold all candidates in memory.

To improve runtime and negate duplicate pair comparison I am storing a `HashSet<Long>` with hashed pair ids which I can check before computing a comparison and skip the pair if it has already been compared.

The assignment was not very clear on if for the final experiments we need the Jaccard similarity or we can just the signature similarity so to compute proper Jaccard similarity I hold the documents in memory which can be improved with streaming if we use the signarute matrix to approximate the similarity.

## Utilities

```bash
# Concatenate java files into one single text
find src -name "*.java" -type f | while read -r file; do
    echo "===== $file ====="
    cat "$file"
    echo
done > all_java_code.txt
```

### Comparing output file pairs

```bash
# Extract just the pair columns (ignore similarity score), sort, compare
cut -f1,2 output_bf.tsv | sort > pairs_bf.txt
cut -f1,2 output_lsh.tsv | sort > pairs_lsh.txt

# Pairs in BF but not LSH (false negatives)
comm -23 pairs_bf.txt pairs_lsh.txt | wc -l

# Pairs in LSH but not BF (false positives)
comm -13 pairs_bf.txt pairs_lsh.txt | wc -l
```

## Results

09.05.2026:
```bash
Running LSH on full dataset
time java -cp .:bin -Xmx2g Runner \
        -method lsh \
        -maxDocs 1000000 \
        -dataFile /home/neji/ku-leuven-bdap-assignment-3/data \
        -outputFile output.tsv \
        -threshold 0.8 \
        -shingleLength 9 \
        -numShingles 1000000 \
        -numHashes 100 \
        -numBands 10 \
        -numBuckets 1000000
Searching items more similar than 0.8 ... 
Initializing hash parameters ... 
done! Took 0.001 seconds.
--------------
Constructing the signature matrix ... 
Read 100000 lines, 98822 valid docs...
Read 200000 lines, 197603 valid docs...
Read 300000 lines, 296463 valid docs...
Read 400000 lines, 395308 valid docs...
Read 500000 lines, 494202 valid docs...
Read 600000 lines, 593061 valid docs...
Read 700000 lines, 691840 valid docs...
Read 800000 lines, 790712 valid docs...
Read 900000 lines, 889534 valid docs...
Read 1000000 lines, 988334 valid docs...
done! Took 360.142 seconds.
--------------
Creating lsh buckets ... 
done! Took 33.613 seconds.
--------------
done! Took 393.764 seconds.
--------------
Found 1828799 similar pairs, saved to 'output.tsv'
--------------
424.40user 8.19system 6:36.16elapsed 109%CPU (0avgtext+0avgdata 1987508maxresident)k
0inputs+1205016outputs (64major+40798minor)pagefaults 0swaps
```

# Report

## Design decisions

To improve runtime and negate duplicate pair comparison I am storing a `HashSet<Long>` with hashed pair ids which I can check before computing a comparison and skip the pair if it has already been compared.



## Experimenting

Test at what point (for the number of shingles) for around 10k docs for brute force does the output not change much anymore in terms of pairs.
The buckets can be set as the max int.



### BF

10k docs 10k shingles - 245 pairs
10k docs 20k shingles - 245 pairs
10k docs 30k shingles - 245 pairs
10k docs 40k shingles - 245 pairs

15k docs 5k shingles - 834 pairs
15k docs 10k shingles - 538 pairs
15k docs 20k shingles - 532 pairs
15k docs 30k shingles - 528 pairs
15k docs 40k shingles - 528 pairs
15k docs 50k shingles - 527 pairs

From the 15k experiments it seems like 30k shingles is a good convergence point, so we continue our experiments with that parameter for LSH.

20k docs 10k shingles - 823 pairs



### LSH

15k docs 30k shingles - 529 pairs 

When comparing the the results for these with BF we get:
    TP=511 FP=18 FN=17
    Precision: 0.9660
    Recall:    0.9678
    F1 score:  0.9669
    Reduction: 0.999995

### Phase 1 Experiments

PHASE 1 SUMMARY:
```
numHashes  numBands  rowsPerBand  approx_threshold  pairs_found  TP   FP  FN  Precision  Recall  F1      Reduction  runtime_s
100        10        10           0.794             529          511  18  17  0.9660     0.9678  0.9669  0.999995   4.8
100        20        5            0.549             538          516  22  12  0.9591     0.9773  0.9681  0.999995   4.8
100        25        4            0.447             538          516  22  12  0.9591     0.9773  0.9681  0.999995   4.7
100        50        2            0.141             538          516  22  12  0.9591     0.9773  0.9681  0.999995   5.1
200        20        10           0.741             529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.6
200        40        5            0.478             529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.7
200        50        4            0.376             529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.7
200        100       2            0.100             529          524  5   4   0.9905     0.9924  0.9915  0.999995   9.2
```

From these results it seems like the number of bands does not affect the metrics as much as the number of hashes does. Clearly higher number of hashes gives us better similarity matching because there is less collisions overall.


### Phase 2 Experiments

Here we changed the bucket count with fixed parameters to test if there is any effect of the bucket numbers on the runs:
- NB_DOCS = 15k
- NB_HASHES = 200
- NB_BANDS = 20

PHASE 2 SUMMARY:

```
numBuckets  pairs_found  TP   FP  FN  Precision  Recall  F1      Reduction  runtime_s
100         529          524  5   4   0.9905     0.9924  0.9915  0.999995   16.6
1000        529          524  5   4   0.9905     0.9924  0.9915  0.999995   8.4
5000        529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.8
10000       529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.8
50000       529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.7
100000      529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.6
500000      529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.6
1000000     529          524  5   4   0.9905     0.9924  0.9915  0.999995   7.6
```

From this we can see that the only thing that is potentially affected is the runtime. 