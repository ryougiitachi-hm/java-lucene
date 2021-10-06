package per.itachi.java.lucene.sample.service;

public interface IndicesService {

    void updateIndices(String url);

    void updateIndicesFromCategoryFolder(String indexName);

    void updateIndicesFromPostFolder(String indexName);
}
