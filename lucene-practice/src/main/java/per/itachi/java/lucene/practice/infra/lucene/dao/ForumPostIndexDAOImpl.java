package per.itachi.java.lucene.practice.infra.lucene.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.practice.common.exception.CommonBusinessException;
import per.itachi.java.lucene.practice.infra.lucene.IndexManager;
import per.itachi.java.lucene.practice.infra.lucene.config.LuceneProperties;
import per.itachi.java.lucene.practice.infra.lucene.entity.PostDoc;

@Slf4j
@Component
public class ForumPostIndexDAOImpl implements ForumPostIndexDAO {

    @Autowired
    private LuceneProperties luceneProperties;

    @Autowired
    private Analyzer smartChineseAnalyzer;

    @Autowired
    private IndexManager indexManager;

    @Override
    public void rebuildIndex(String indexName, String newIndexName) {
        Path idxPath = Paths.get(luceneProperties.getBasePath(), indexName);
        Path newIdxPath = Paths.get(luceneProperties.getBasePath(), newIndexName);
        if (Files.exists(newIdxPath)) {
            try {
                Files.createDirectories(newIdxPath);
            }
            catch (IOException e) {
                log.error("Error occurred. ", e);
                throw new CommonBusinessException(e);
            }
        }

        IndexWriterConfig writerConfig = new IndexWriterConfig(this.smartChineseAnalyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setCommitOnClose(true);

        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(idxPath));
            IndexWriter writer = new IndexWriter(FSDirectory.open(newIdxPath), writerConfig) ) {
            Query query = new MatchAllDocsQuery();
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, reader.numDocs());
            log.info("There are {} documents to rebuild. ", topDocs.totalHits.value);
            log.info("Started rebuilding. ");
            int count = 0;
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                ++count;
                Document document = searcher.doc(scoreDoc.doc);
                document.add(new NumericDocValuesField(PostDoc.FLD_CATEGORY_ID,
                        indexManager.getValueFromNumericDocValues(reader, scoreDoc.doc, PostDoc.FLD_CATEGORY_ID)));
                document.add(new NumericDocValuesField(PostDoc.FLD_POST_ID,
                        indexManager.getValueFromNumericDocValues(reader, scoreDoc.doc, PostDoc.FLD_POST_ID)));
                writer.addDocument(document);
                if (count % luceneProperties.getCommitDocThreshold() == 0) {
                    writer.flush();
                    log.info("Rebuilt documents which are ended with {}. ", scoreDoc.doc);
                }
            }
            log.info("Rebuilt documents, count={}. ", count);
            log.info("Completed rebuilding. ");
//            logger.info("topDocs.totalHits: {}", topDocs.totalHits);
        }
        catch (IOException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }
}
