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