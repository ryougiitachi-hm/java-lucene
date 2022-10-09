package per.itachi.java.lucene.practice.infra.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
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
    private AnalyzerInitializer analyzerInitializer;

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
            Analyzer analyzer = new SmartChineseAnalyzer();
            analyzerInitializer.initialize(analyzer);
            // if specific field name doesn't exist, there is no any error.
            QueryParser parser = new QueryParser(PostDoc.FLD_CONTENT, analyzer);
            Query query = parser.parse(keyword);
            TopDocs topDocs = searcher.search(query, 1000);
//            logger.info("topDocs: {}", topDocs);
//            logger.info("topDocs.totalHits: {}", topDocs.totalHits);
            return assemblePaginationDoc(searcher, topDocs, pageSize, pageNbr);
        }
        catch (IOException | ParseException e) {
            log.error("", e);
            throw new CommonBusinessException(e);
        }
    }

    private PaginationDoc<PostDoc> assemblePaginationDoc(IndexSearcher searcher, TopDocs topDocs, int pageSize, int pageNbr)
            throws IOException {
        ScoreDoc[] scoreDoc = topDocs.scoreDocs;
        List<PostDoc> results = new ArrayList<>(pageSize);
        for (int i = (pageNbr - 1) * pageSize; i < scoreDoc.length; ++i) {
            Document document = searcher.doc(scoreDoc[i].doc);
            PostDoc doc = PostDoc.builder()
                    .categoryId(document.getField(PostDoc.FLD_CATEGORY_ID).numericValue().longValue())
                    .postId(document.getField(PostDoc.FLD_POST_ID).numericValue().longValue())
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
}
