package per.itachi.java.lucene.practice.joint.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import per.itachi.java.lucene.practice.app.dto.ForumPostDTO;
import per.itachi.java.lucene.practice.app.dto.PaginationDTO;
import per.itachi.java.lucene.practice.app.service.ForumPostService;

@RestController
@RequestMapping("/forum")
public class PostController {

    @Autowired
    private ForumPostService forumPostService;

    @GetMapping("/posts/index-info")
    public void printIndexBasicInfo(@RequestParam String indexName) {
        forumPostService.printIndexBasicInfo(indexName);
    }

    @GetMapping("/posts")
    public PaginationDTO<ForumPostDTO> queryPostsByPage(@RequestParam String indexName,
                                                        @RequestParam String keyword,
                                                        @RequestParam int pageSize,
                                                        @RequestParam int pageNbr) {
        return forumPostService.queryDocumentsByPage(indexName, keyword, pageSize, pageNbr);
    }

    @PostMapping("/posts/rebuild")
    public void rebuildIndex(@RequestParam String indexName, @RequestParam String newIndexName) {
        forumPostService.rebuildIndex(indexName, newIndexName);
    }
}
