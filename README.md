# Hello Lucene

I’ve been interested in studying search engines for a while, ever since I had the opportunity to work on a project that integrated with Solr. Since both Solr and Elasticsearch are built on top of Lucene, I thought it would be a good idea to start by understanding how Lucene works.

I found [LuceneTutorial.com](https://www.lucenetutorial.com/index.html), which references the article **Lucene in 5 minutes** , and I decided to replicate this project.

# My Hello Lucene

For this project, I decided to simulate an e-commerce, so I'm indexing products:

```java
IndexWriter writer = new IndexWriter(index, config);
addDoc(writer, "TV", "1");
addDoc(writer, "Smart TV", "2");
addDoc(writer, "Smartphone", "3");
addDoc(writer, "Cellphone", "4");
addDoc(writer, "Case for Smartphone", "5");
addDoc(writer, "Smart TV 4K", "6");
writer.close();
```

And then, I search for `tv` and have the following output:
```text
Found 3 products.
1 - TV
2 - Smart TV
3 - Smart TV 4K
```

Let's search for `smartphone`:
```text
Found 2 products.
1 - Smartphone
2 - Case for Smartphone
```

Just `phone`: 
```text
Found 0 products.
```

# Lucene Search Query Syntax

Before learning more about the query syntax , let's add the `department` field:

```java
 private static void addDoc(IndexWriter writer, String productName, String department, String sku) throws IOException{
    Document doc=new Document();
    doc.add(new TextField("product",productName,Field.Store.YES));
    doc.add(new TextField("department",department,Field.Store.YES));
    ...
}
```

And improve our documents:
```java
IndexWriter writer = new IndexWriter(index, config);		
addDoc(writer, "TV", "TVs", "1");
addDoc(writer, "Smart TV", "TVs","2");
addDoc(writer, "Smartphone", "Smartphones", "3");
addDoc(writer, "Cellphone", "Smartphones", "4");
addDoc(writer, "Case for Smartphone", "Smartphone Cases & Covers","5");
addDoc(writer, "Smart TV 4K", "TVs", "6");
addDoc(writer, "TV Wall Bracket", "TV Accessories", "7");
writer.close();
```

### Default Field

Now, let's understand how the QueryParser works.

We specified `product` as the default field. So it is searching for `tv` in the field `product`, not in `department` or `sku` fields:

```java
    String queryString = "tv";

    Query query = null;
    try {
        query = new QueryParser("product", analyzer).parse(queryString);
    } catch (ParseException e) {
        throw new RuntimeException(e);
    }
```
Output:
```text
Found 4 products.
1 - TV in TVs
2 - Smart TV in TVs
3 - Smart TV 4K in TVs
4 - TV Wall Bracket in TV Accessories
```

We can specify the field `department` instead:
```java
String queryString = "accessories";

Query query = null;
try {
    query = new QueryParser("department", analyzer).parse(queryString);
} catch (ParseException e) {
    throw new RuntimeException(e);
}
```
Output:
```text
Found 1 products.
1 - TV Wall Bracket in TV Accessories
```
If we search for `accessories` but the default field is `product`, the result is `0`:
```java
String queryString = "accessories";

Query query = null;
try {
    query = new QueryParser("product", analyzer).parse(queryString);
} catch (ParseException e) {
    throw new RuntimeException(e);
}
```
Output:
```text
Found 0 products.
```
In the following examples, we are going to specify the fields in a more dynamic way.

### Keyword matching

Let's keep "product" as the default field:
```java
new QueryParser("product", analyzer).parse(queryString);
```
Let's print the query too:
```java
System.out.println("Found " + scoreDocs.length + " results for: '" + queryString + "'.");
```
And see some examples: 

1) Searching for the **word** `tv` in the field `product`:
```text
Found 4 results for: 'product:tv'.
1 - TV in TVs
2 - Smart TV in TVs
3 - Smart TV 4K in TVs
4 - TV Wall Bracket in TV Accessories
```

2. Search for phrase:
```text
Found 4 results for: 'product:smart tv'.
1 - Smart TV in TVs
2 - Smart TV 4K in TVs
3 - TV in TVs
4 - TV Wall Bracket in TV Accessories
```

3. Using `AND` clause:
```text
Found 3 results for: 'product:tv AND department:TVs'.
1 - TV in TVs
2 - Smart TV in TVs
3 - Smart TV 4K in TVs
```

4. Using `OR` clause:
```text
Found 6 results for: 'product:tv OR product:smartphone'.
1 - Smartphone in Smartphones
2 - Case for Smartphone in Smartphone Cases & Covers
3 - TV in TVs
4 - Smart TV in TVs
5 - Smart TV 4K in TVs
6 - TV Wall Bracket in TV Accessories
```

5. Using exclusion:
```text
Found 1 results for: 'product:smartphone -department:cases'.
1 - Smartphone in Smartphones
```

### Wildcard Matching

1. Word that starts with:
```text
Found 4 results for: 'product:smart*'.
1 - Smart TV in TVs
2 - Smartphone in Smartphones
3 - Case for Smartphone in Smartphone Cases & Covers
4 - Smart TV 4K in TVs
```
2. Word that starts with and end with:
```text
Found 2 results for: 'product:sm*phone'.
1 - Smartphone in Smartphones
2 - Case for Smartphone in Smartphone Cases & Covers
```
In the [tutorial](https://www.lucenetutorial.com/lucene-query-syntax.html) there is this note: 

"Note that Lucene doesn't support using a * symbol as the first character of a search."

### Proximity matching
To understand better how it works see the explanation in the [tutorial](https://www.lucenetutorial.com/lucene-query-syntax.html):

1. Zero words from each other (between `smart` and `4k`):
```text
Found 0 results for: '"smart 4k"~0'.
```
2. One word from each other:
```text
Found 1 results for: '"smart 4k"~1'.
1 - Smart TV 4K in TVs
```
3. Word transposition
```text
Found 0 results for: '"4k tv"~1'.
```
The tutorial says: "Note that for proximity searches, exact matches are proximity zero, and word transpositions (bar foo) are proximity 1", so I was expecting to have the "Smart TV 4K in TVs" returned. I don't know if I misunderstood it, but it's something that I'll need more time to understand it.

### Ranges Searches

```text
Found 4 results for: 'sku:[2 TO 5]'.
1 - Smart TV in TVs
2 - Smartphone in Smartphones
3 - Cellphone in Smartphones
4 - Case for Smartphone in Smartphone Cases & Covers
```

### Boosts

1. Specifying that `product tv` in `department:tvs` is more important than `product:tv` in `department:tv accessories`: 
```text
Found 4 results for: '(product:tv AND department:tvs)^1.5 (product:tv AND department:tv accessories)'.
1 - TV in TVs
2 - Smart TV in TVs
3 - Smart TV 4K in TVs
4 - TV Wall Bracket in TV Accessories
```
2. Specifying that `department:tv accessories` is more important now:
```text
Found 4 results for: '(product:tv AND department:tv accessories)^1.5 (product:tv AND department:tvs)'.
1 - TV Wall Bracket in TV Accessories
2 - TV in TVs
3 - Smart TV in TVs
4 - Smart TV 4K in TVs
```

## Next Steps
1. Query classes
2. Scoring

[Lucene Doc](https://lucene.apache.org/core/4_0_0/core/org/apache/lucene/search/package-summary.html#search)