package per.itachi.java.lucene.sample.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class ForumProperties {

    private String name;

    /**
     * lucene's index name and index directory name
     * */
    @JsonProperty("index-name")
    private String indexName;

    private String charset;

    @JsonProperty("interval-fixed")
    private int intervalFixed;

    @JsonProperty("interval-flexible")
    private int intervalFlexible;

    private List<String> domains;

    @JsonProperty("forum-categories-selector")
    private String forumCategoriesSelector;

    @JsonProperty("category-post-list-selector")
    private String categoryPostListSelector;

    @JsonProperty("category-post-inline-title-selector")
    private String categoryPostInlineTitleSelector;

    @JsonProperty("category-post-inline-url-selector")
    private String categoryPostInlineUrlSelector;

    @JsonProperty("category-post-inline-cdate-selector")
    private String categoryPostInlineCdateSelector;

    @JsonProperty("category-post-inline-edate-selector")
    private String categoryPostInlineEdateSelector;

    @JsonProperty("category-next-page-selector")
    private String categoryNextPageSelector;

    /**
     * used to generate file name for category html
     * */
    @JsonProperty("category-params")
    private List<String> categoryParams;

    /**
     * used to generate file name for post html
     * */
    @JsonProperty("post-params")
    private List<String> postParams;

    private Map<String, String> headers;
}
