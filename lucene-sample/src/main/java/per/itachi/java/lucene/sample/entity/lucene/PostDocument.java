package per.itachi.java.lucene.sample.entity.lucene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
public class PostDocument {

    public static final String FLD_POST_ID = "post_id";

    public static final String FLD_FILE_NAME = "file_name";

    public static final String FLD_FILE_PATH = "file_path";

    public static final String FLD_CDATE = "cdate";

    public static final String FLD_EDATE = "edate";

    public static final String FLD_CONTENT = "content";

    /**
     * the identifier of the current post document, sometimes threadId etc.
     * */
    private Long postId;

    private String title;

    private String fileName;

    private String filePath;

    private OffsetDateTime cdate;

    private OffsetDateTime edate;
}
