package com.khirata.examples;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

public class QueryClassesExamples {

    public static void main(String[] args) throws IOException {

        // MUST
        Query tvQuery = new TermQuery(new Term("product", "tv"));

        Query departmentTvsQuery = new TermQuery(new Term("department", "tvs"));

        Query tvInTvsQuery = new BooleanQuery.Builder()
                .add(tvQuery, BooleanClause.Occur.MUST)
                .add(departmentTvsQuery, BooleanClause.Occur.MUST)
                .build();
        search(tvInTvsQuery);

        // SHOULD
        TermQuery smartphoneQuery = new TermQuery(new Term("product", "smartphone"));
        TermQuery cellphoneQuery = new TermQuery(new Term("product", "cellphone"));

        BooleanQuery smartOrCellPhoneQuery = new BooleanQuery.Builder()
                .add(smartphoneQuery, BooleanClause.Occur.SHOULD)
                .add(cellphoneQuery, BooleanClause.Occur.SHOULD)
                .build();
        search(smartOrCellPhoneQuery);

        // MUST AND MUST NOT
        Query tvNotInTvsQuery = new BooleanQuery.Builder()
                .add(tvQuery, BooleanClause.Occur.MUST)
                .add(departmentTvsQuery, BooleanClause.Occur.MUST_NOT)
                .build();
        search(tvNotInTvsQuery);
    }

    public static void search(Query query) throws IOException {

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

        // 2. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] scoreDocs = docs.scoreDocs;

        // 4. display the results
        System.out.println("Found " + scoreDocs.length + " results for: '" + query.toString() + "'.");
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
