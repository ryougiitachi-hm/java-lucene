package per.itachi.java.lucene.practice.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import per.itachi.java.lucene.practice.app.dto.ForumPostDTO;
import per.itachi.java.lucene.practice.app.dto.PaginationDTO;
import per.itachi.java.lucene.practice.app.service.ForumPostService;
import per.itachi.java.lucene.practice.infra.lucene.IndexManager;
import per.itachi.java.lucene.practice.infra.lucene.entity.PaginationDoc;
import per.itachi.java.lucene.practice.infra.lucene.entity.PostDoc;

@Slf4j
@Service
public class ForumPostServiceImpl implements ForumPostService {

    @Resource
    private IndexManager indexManager;

    @Override
    public PaginationDTO<ForumPostDTO> queryDocumentsByPage(String indexName, String keyword, int pageSize, int pageNbr) {
        PaginationDoc<PostDoc> postDocList = indexManager.queryDocumentsByPage(indexName, keyword, pageSize, pageNbr);
        List<ForumPostDTO> data = new ArrayList<>(postDocList.getPageSize());
        for(PostDoc postDoc : postDocList.getDocuments()) {
            ForumPostDTO dto = new ForumPostDTO();
            BeanUtils.copyProperties(postDoc, dto);
            data.add(dto);
        }
        PaginationDTO<ForumPostDTO> paginationDTO = new PaginationDTO<>();
        BeanUtils.copyProperties(postDocList, paginationDTO);
        paginationDTO.setData(data);
        return paginationDTO;
    }
}
