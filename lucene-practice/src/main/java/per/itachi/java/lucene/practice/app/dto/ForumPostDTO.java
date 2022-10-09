package per.itachi.java.lucene.practice.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumPostDTO {

    private Long categoryId;

    private Long postId;

    private String title;

    private String fileName;

    private String filePath;
}
