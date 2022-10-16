package per.itachi.java.lucene.practice.infra.lucene;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import per.itachi.java.lucene.practice.infra.lucene.entity.PaginationDoc;
import per.itachi.java.lucene.practice.infra.lucene.entity.PostDoc;

public interface IndexManager {

    void printIndexBasicInfo(String indexName);

    void addDocument(String indexName, PostDoc postDoc);

    void addDocuments(String indexName, List<PostDoc> postDoc);

    void deleteDocument(String indexName, String syntax);

    void replaceDocument(String indexName, String syntax, PostDoc postDoc);

    /**
     * Temporarily, dedicate to postdoc.
     * @param indexName namely website name.
     * @param pageSize 10, 20, 50 are recommended.
     * @param pageNbr 1-based
     * @return
     * */
    PaginationDoc<PostDoc> queryDocumentsByPage(String indexName, String syntax, int pageSize, int pageNbr);

    long getValueFromNumericDocValues(IndexReader reader, int docID, String fieldName) throws IOException;
}
