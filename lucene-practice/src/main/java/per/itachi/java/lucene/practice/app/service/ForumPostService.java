package per.itachi.java.lucene.practice.app.service;

import per.itachi.java.lucene.practice.app.dto.ForumPostDTO;
import per.itachi.java.lucene.practice.app.dto.PaginationDTO;

public interface ForumPostService {

    PaginationDTO<ForumPostDTO> queryDocumentsByPage(String indexName, String keyword, int pageSize, int pageNbr);
}
