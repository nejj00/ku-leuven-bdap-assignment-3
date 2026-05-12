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