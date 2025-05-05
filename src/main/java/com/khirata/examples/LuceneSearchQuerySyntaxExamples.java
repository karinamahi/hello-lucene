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
        // search for word
        search("product:tv");
        search("department:accessories");

        // search for phrases
        search("product:smart tv");

        // using "AND" clause
        search("product:tv AND department:TVs" );

        // using "OR" clause
        search("product:tv OR product:smartphone");

        // using exclusion
        search("product:smartphone -department:cases");

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
