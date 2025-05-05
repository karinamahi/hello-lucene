package com.khirata.examples;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

public class LuceneSearchQuerySyntaxExamples {


    public static void main(String[] args) throws IOException {
        // keyword matching examples
        // 1. search for word
        search("product:tv");
        search("department:accessories");

        // 2. search for phrases
        search("product:smart tv");

        // 3. using "AND" clause
        search("product:tv AND department:TVs" );

        // 4. using "OR" clause
        search("product:tv OR product:smartphone");

        // 5. using exclusion
        search("product:smartphone -department:cases");

        // wildcard matching
        // 1. word that starts with
        search("product:smart*");

        // 2. word that starts with and ends with
        search("product:sm*phone");

        // proximity matching
        // 1. zero words from each other
        search("\"smart 4k\"~0");

        // 2. one word from each other
        search("\"smart 4k\"~1");

        // 3. transposition
        search("\"4k tv\"~1");

        // ranges searches
        search("sku:[2 TO 5]");

        // boosts
        search("(product:tv AND department:tvs)^1.5 (product:tv AND department:tv accessories)");

        search("(product:tv AND department:tv accessories)^1.5 (product:tv AND department:tvs)");

    }

    public static void search(String queryString) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer();

        //1. create the index
        ByteBuffersDirectory index = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer = new IndexWriter(index, config);
        addDoc(writer, "TV", "TVs", "1");
        addDoc(writer, "Smart TV", "TVs","2");
        addDoc(writer, "Smartphone", "Smartphones", "3");
        addDoc(writer, "Cellphone", "Smartphones", "4");
        addDoc(writer, "Case for Smartphone", "Smartphone Cases & Covers","5");
        addDoc(writer, "Smart TV 4K", "TVs", "6");
        addDoc(writer, "TV Wall Bracket", "TV Accessories", "7");
        writer.close();

        //2. create the query

        Query query = null;
        try {
            query = new QueryParser("product", analyzer).parse(queryString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] scoreDocs = docs.scoreDocs;

        // 4. display the results
        System.out.println("Found " + scoreDocs.length + " results for: '" + queryString + "'.");
        for (int i = 0; i < scoreDocs.length; i++) {
            int docId = scoreDocs[i].doc;
            Document document = searcher.storedFields().document(docId);
            System.out.println((i + 1) + " - " + document.get("product") + " in " + document.get("department"));
        }
        System.out.println();

        reader.close();
    }

    private static void addDoc(IndexWriter writer, String productName, String department, String sku) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("product", productName, Field.Store.YES));
        doc.add(new TextField("department", department, Field.Store.YES));

        // using a StringField because we don't want it tokenized
        doc.add(new StringField("sku", sku, Field.Store.YES));
        writer.addDocument(doc);
    }
}
