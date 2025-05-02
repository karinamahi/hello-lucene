# Hello Lucene

I’ve been interested in studying search engines for a while, ever since I had the opportunity to work on a project that integrated with Solr. Since both Solr and Elasticsearch are built on top of Lucene, I thought it would be a good idea to start by understanding how Lucene works.

I found [LuceneTutorial.com](https://www.lucenetutorial.com/index.html), which references the article **Lucene in 5 minutes** , and I decided to replicate this project.

## My Hello Lucene

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
And then, I search for "tv" and have the following output:
```text
Found 3 products.
1 - TV
2 - Smart TV
3 - Smart TV 4K
```

Let's search for "smartphone":
```text
Found 2 products.
1 - Smartphone
2 - Case for Smartphone
```

Just "phone": 
```text
Found 0 products.
```
