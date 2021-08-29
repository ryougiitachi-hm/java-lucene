package per.itachi.java.lucene.sample.persist;

import per.itachi.java.lucene.sample.entity.lucene.PostDocument;

import java.util.List;

/**
 * <p>
 * There appears to be 2 index writing mode: overwrite, append.
 * overwrite
 *      overwrite all of contents each time.
 * append
 *      check whether each post was edited recently before updating.
 * Confused: this writing mode should be added to this layer?
 * </p>
 * */
public interface CommonIndexManager {

    void writeIndices(String indexName, PostDocument document);

    void writeIndices(String indexName, List<PostDocument> documents);
}
