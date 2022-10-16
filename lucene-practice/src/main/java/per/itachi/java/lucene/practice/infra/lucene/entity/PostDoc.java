package per.itachi.java.lucene.practice.infra.lucene.entity;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDoc extends BaseDoc {

    public static final String FLD_CATEGORY_ID = "category_id";

    public static final String FLD_POST_ID = "post_id";

    public static final String FLD_FILE_NAME = "file_name";

    public static final String FLD_FILE_PATH = "file_path";

    public static final String FLD_CDATE = "cdate";

    public static final String FLD_EDATE = "edate";

    public static final String FLD_CONTENT = "content";

    private Long categoryId;

    /**
     * the identifier of the current post document, sometimes threadId etc.
     * */
    private Long postId;

    private String title;

    private String fileName;

    private String filePath;

    private OffsetDateTime cdate;

    private OffsetDateTime edate;

    private String content;
}