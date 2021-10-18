package per.itachi.java.lucene.sample.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.entity.lucene.PostDocument;

@Repository
public class LuceneIndexManager implements CommonIndexManager {

    private static final int PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(LuceneIndexManager.class);

    @Autowired
    private ConfigurationContext configurationContext;

    @Override
    public void writeIndices(String indexName, PostDocument document) {
    }

    @Override
    public void writeIndices(String indexName, List<PostDocument> documents) {
        Path idxPath = Paths.get(configurationContext.getIndexDirectory(), indexName);
        // check whether exists, if not, create directory.
        if (!Files.exists(idxPath)) {
            logger.warn("The specific index {} doesn't have the corresponding path {}.", indexName, idxPath);
            try {
                Files.createDirectory(idxPath);// FileAttribute
            }
            catch (IOException e) {
                logger.error("Error occured when creating index directory {}. ", idxPath, e);
                return;
            }
        }

        // check whether it is directory, if not, return.
        if (!Files.isDirectory(idxPath)) {
            logger.error("The specific index {} path {} has existed, and is a directory.", indexName, idxPath);
            return;
        }

        List<PostDocument> documentsToUpdate = new LinkedList<>();// actually, to delete
        List<PostDocument> documentsToAdd = new LinkedList<>();

        // initialise analyzer.
        Analyzer analyzer = new SmartChineseAnalyzer();

        // traverse list of the documents.
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(idxPath))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            for (PostDocument document: documents) {
//                Query query = new TermQuery(new Term(PostDocument.FLD_POST_ID,
//                        String.valueOf(document.getPostId())));
                // if specific field name doesn't exist, there is no any error.
//                QueryParser parser = new QueryParser(PostDocument.FLD_POST_ID, analyzer);
//                Query query = parser.parse(String.valueOf(document.getPostId()));
                // NumericRangeQuery SortedNumericDocValuesRangeQuery
                Query query = NumericDocValuesField
                        .newSlowExactQuery(PostDocument.FLD_POST_ID, document.getPostId());
                TopDocs topDocs = searcher.search(query, PAGE_SIZE);
                if (topDocs.scoreDocs.length > 0) {
                    logger.info("The post {} exists, fileName={}, topDocs.scoreDocs.length={}, topDocs.totalHits={}. ",
                            document.getPostId(), document.getFileName(), topDocs.scoreDocs.length, topDocs.totalHits);
                    documentsToUpdate.add(document);
                    documentsToAdd.add(document);
                }
                else {
                    logger.info("The post {} doesn't exist, fileName={}, topDocs.scoreDocs.length={}, topDocs.totalHits={}. ",
                            document.getPostId(), document.getFileName(), topDocs.scoreDocs.length, topDocs.totalHits);
                    documentsToAdd.add(document);
                }
            }
        }
        catch (IOException e) {
            logger.error("", e);
        }

        logger.info("There are {} documents to delete, {} documents to add. ",
                documentsToUpdate.size(), documentsToAdd.size());

        Sort sortPostId = new Sort(new SortField(PostDocument.FLD_POST_ID, SortField.Type.LONG, false));
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writerConfig.setIndexSort(sortPostId);
        writerConfig.setMergeScheduler(new ConcurrentMergeScheduler());// to be checked
        writerConfig.setCommitOnClose(true);
//        writerConfig.setIndexCommit()
//        writerConfig.setIndexDeletionPolicy();

        try(Directory dirIndex = FSDirectory.open(idxPath);
            IndexWriter writer = new IndexWriter(dirIndex, writerConfig)) {
            removeExpiredDocuments(writer, documentsToUpdate);
            addUpdatedDocuments(writer, documentsToAdd);
        }
        catch (IOException e) {
            logger.error("", e);
        }
    }

    private void removeExpiredDocuments(IndexWriter writer, List<PostDocument> documents)
            throws IOException {
        int count = 0;
        for(PostDocument postDocument : documents) {
            ++count;
            logger.info("[Index Mgr] Removing document {}. ", count);
//            writer.deleteDocuments(new Term(PostDocument.FLD_POST_ID,
//                    String.valueOf(postDocument.getPostId())));
            Query query = NumericDocValuesField
                    .newSlowExactQuery(PostDocument.FLD_POST_ID, postDocument.getPostId());
            writer.deleteDocuments(query);
            logger.info("[Index Mgr] Finished removing document {}. ", count);
        }
        writer.commit();
    }

    private void addUpdatedDocuments(IndexWriter writer, List<PostDocument> documents)
            throws IOException {
        int count = 0;
        for (PostDocument postDocument : documents) {
            try(BufferedReader br = Files.newBufferedReader(Paths.get(postDocument.getFilePath()))) {
                ++count;
                logger.info("[Index Mgr] Adding document {}. ", count);
                StringBuilder builder = new StringBuilder(1 << 15);
                String line = null;
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }
                Document document = new Document();
                document.add(new NumericDocValuesField(PostDocument.FLD_CATEGORY_ID,
                        postDocument.getCategoryId()));
//                document.add(new StringField(PostDocument.FLD_POST_ID,
//                        String.valueOf(postDocument.getPostId()), Field.Store.YES));
                document.add(new NumericDocValuesField(PostDocument.FLD_POST_ID,
                        postDocument.getPostId()));
                document.add(new TextField(PostDocument.FLD_FILE_NAME,
                        postDocument.getFileName(), Field.Store.YES));
                document.add(new TextField(PostDocument.FLD_FILE_PATH,
                        postDocument.getFilePath(), Field.Store.YES));
                document.add(new TextField(PostDocument.FLD_CONTENT,
                        builder.toString(), Field.Store.YES));
                writer.addDocument(document);
                logger.info("[Index Mgr] Finished adding document {}. ", count);
            }
            catch (IOException e) {
                logger.error("Failed to write post {}.", postDocument.getPostId(), e);
            }
        }
        writer.commit();
    }
}