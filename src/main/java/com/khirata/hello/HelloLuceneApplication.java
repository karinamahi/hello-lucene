package com.khirata.hello;

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

public class HelloLuceneApplication {

	public static void main(String[] args) throws IOException {

		StandardAnalyzer analyzer = new StandardAnalyzer();

		//1. create the index
		ByteBuffersDirectory index = new ByteBuffersDirectory();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter writer = new IndexWriter(index, config);
		addDoc(writer, "TV", "1");
		addDoc(writer, "Smart TV", "2");
		addDoc(writer, "Smartphone", "3");
		addDoc(writer, "Cellphone", "4");
		addDoc(writer, "Case for Smartphone", "5");
		addDoc(writer, "Smart TV 4K", "6");
		writer.close();

		//2. create the query
		String queryString = "phone";

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
		System.out.println("Found " + scoreDocs.length + " products.");
		for (int i = 0; i < scoreDocs.length; i++) {
			int docId = scoreDocs[i].doc;
			Document document = searcher.storedFields().document(docId);
//			System.out.println((i + 1) + " - " + document.get("product") + " - sku: " + document.get("sku"));
			System.out.println((i + 1) + " - " + document.get("product"));
		}

		reader.close();
	}

	private static void addDoc(IndexWriter writer, String productName, String sku) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("product", productName, Field.Store.YES));

		// using a StringField because we don't want it tokenized
		doc.add(new StringField("sku", sku, Field.Store.YES));
		writer.addDocument(doc);
	}
}
