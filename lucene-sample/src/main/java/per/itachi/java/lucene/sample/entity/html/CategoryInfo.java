package per.itachi.java.lucene.sample.entity.html;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class CategoryInfo {

    private String fileName;
    
    private String fid;

    private List<PostInfo> postInfos = Collections.emptyList();

    private String nextPageUrl;
}
