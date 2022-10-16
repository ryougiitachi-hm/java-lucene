package per.itachi.java.lucene.practice.infra.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.practice.common.exception.CommonBusinessException;
import per.itachi.java.lucene.practice.infra.lucene.config.LuceneProperties;
import per.itachi.java.lucene.practice.infra.lucene.entity.PaginationDoc;
import per.itachi.java.lucene.practice.infra.lucene.entity.PostDoc;

@Slf4j
@Component
public class IndexManagerImpl implements IndexManager{

    @Autowired
    private LuceneProperties luceneProperties;

    @Autowired
    private Analyzer smartChineseAnalyzer;

    @Override
    public void printIndexBasicInfo(String indexName) {
        Path idxPath = Paths.get(luceneProperties.getBasePath(), indexName);
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(idxPath)) ) {
            log.info("The index name is {}, numDocs={}.", indexName, reader.numDocs());
            log.info("The index name is {}, maxDoc={}.", indexName, reader.maxDoc());
        }
        catch (IOException e) {
            log.error("", e);
            throw new CommonBusinessException(e);
        }
    }

    @Override
    public void addDocument(String indexName, PostDoc postDoc) {
    }

    @Override
    public void addDocuments(String indexName, List<PostDoc> postDoc) {
    }

    @Override
    public void deleteDocument(String indexName, String syntax) {
    }

    @Override
    public void replaceDocument(String indexName, String syntax, PostDoc postDoc) {
    }

    @Override
    public PaginationDoc<PostDoc> queryDocumentsByPage(String indexName, String keyword, int pageSize, int pageNbr) {
        Path idxPath = Paths.get(luceneProperties.getBasePath(), indexName);
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(idxPath)) ) {
            IndexSearcher searcher = new IndexSearcher(reader);
            // if specific field name doesn't exist, there is no any error.
            QueryParser parser = new QueryParser(PostDoc.FLD_CONTENT, smartChineseAnalyzer);
            Query query = parser.parse(keyword);
            TopDocs topDocs = searcher.search(query, 1000);
//            logger.info("topDocs: {}", topDocs);
//            logger.info("topDocs.totalHits: {}", topDocs.totalHits);
            return assemblePaginationDoc(reader, searcher, topDocs, pageSize, pageNbr);
        }
        catch (IOException | ParseException e) {
            log.error("", e);
            throw new CommonBusinessException(e);
        }
    }

    private PaginationDoc<PostDoc> assemblePaginationDoc(IndexReader reader, IndexSearcher searcher,
                                                         TopDocs topDocs, int pageSize, int pageNbr)
            throws IOException {
        ScoreDoc[] scoreDoc = topDocs.scoreDocs;
        List<PostDoc> results = new ArrayList<>(pageSize);
        for (int i = (pageNbr - 1) * pageSize; i < scoreDoc.length; ++i) {
            Document document = searcher.doc(scoreDoc[i].doc); // this set of fields is optional.
            long categoryId = getValueFromNumericDocValues(reader, scoreDoc[i].doc, PostDoc.FLD_CATEGORY_ID);
            long postId = getValueFromNumericDocValues(reader, scoreDoc[i].doc, PostDoc.FLD_POST_ID);
            PostDoc doc = PostDoc.builder()
                    .categoryId(categoryId)
                    .postId(postId)
//                    .title(document.get(PostDoc.FL))
                    .fileName(document.get(PostDoc.FLD_FILE_NAME))
                    .filePath(document.get(PostDoc.FLD_FILE_PATH))
                    .content(document.get(PostDoc.FLD_CONTENT))
                    .build();
            results.add(doc);
        }
        PaginationDoc<PostDoc> pagination = PaginationDoc.<PostDoc>builder()
                .totalCount(scoreDoc.length)
                .pageSize(pageSize)
                .pageNbr(pageNbr)
                .documents(results)
                .build();
        return pagination;
    }

    @Override
    public long getValueFromNumericDocValues(IndexReader reader, int docID, String fieldName) throws IOException {
        LeafReaderContext leafReaderContext = retrieveLeafReader(reader.leaves(), docID);
        NumericDocValues docValues = DocValues.getNumeric(leafReaderContext.reader(), fieldName);
        docValues.advance(docID - leafReaderContext.docBase);
        return docValues.longValue();
    }

    private LeafReaderContext retrieveLeafReader(List<LeafReaderContext> leaves, int docID) {
        int idxLeafReader = locateLeafReader(leaves, docID);
        return leaves.get(idxLeafReader);
    }

    /**
     * Use default level because of convenience for integration test.
     * */
    int locateLeafReader(List<LeafReaderContext> leaves, int docID) {
        int left = 0;
        int right = leaves.size() - 1;
        int mid = 0;
        while (left < right) {
            mid = (left + right) / 2;
            int docBase = leaves.get(mid).docBase;
            if (docID < docBase) {
                right = mid - 1;
            }
            else if (docID > docBase) { // improvable and to be improved
                left = mid + 1;
            }
            else {
                return mid;
            }
        }
        // it is necessary to correct and adjust the left and right range
        while (mid < leaves.size() - 1 && docID >= leaves.get(mid + 1).docBase) {
            ++mid;
        }
        while (mid >= 1 && docID < leaves.get(mid).docBase) {
            --mid;
        }
        return mid;
    }
}
