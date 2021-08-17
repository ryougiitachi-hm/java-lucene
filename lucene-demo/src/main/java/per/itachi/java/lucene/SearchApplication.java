package per.itachi.java.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class SearchApplication {

    private static final Logger logger = LoggerFactory.getLogger(SearchApplication.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("the length of arguments is less than 1. ");
            return;
        }

        String strIdxPath = "indices";
        String strQueryString = args[0];
        String strFldName = "content";

        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(strIdxPath)));) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            // if specific field name doesn't exist, there is no any error.
            QueryParser parser = new QueryParser(strFldName, analyzer);
            Query query = parser.parse(strQueryString);
            TopDocs topDocs = searcher.search(query, 1000);
            logger.info("topDocs: {}", topDocs);
            logger.info("topDocs.totalHits: {}", topDocs.totalHits);
            showScoreDocs(topDocs.scoreDocs, searcher);
        }
        catch (IOException | ParseException e) {
            logger.error("", e);
        }

    }

    private static void showScoreDocs(ScoreDoc[] scoreDocs, IndexSearcher searcher)
            throws IOException {
        logger.info("showScoreDocs: ");
        for (ScoreDoc scoreDoc : scoreDocs) {
            logger.info("ScoreDoc: {}", scoreDoc);
            Document document = searcher.doc(scoreDoc.doc);
            logger.info("scoreDoc.doc file_name={}", document.get("file_name"));
        }
    }
}
