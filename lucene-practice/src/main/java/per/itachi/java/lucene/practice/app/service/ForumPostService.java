package per.itachi.java.lucene.practice.app.service;

import per.itachi.java.lucene.practice.app.dto.ForumPostDTO;
import per.itachi.java.lucene.practice.app.dto.PaginationDTO;

public interface ForumPostService {

    void printIndexBasicInfo(String indexName);

    PaginationDTO<ForumPostDTO> queryDocumentsByPage(String indexName, String keyword, int pageSize, int pageNbr);

    void rebuildIndex(String indexName, String newIndexName);

    void rebuildIndexWithCustom(String indexName, String newIndexName);
}
