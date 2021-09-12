package per.itachi.java.lucene.sample.parser;

import per.itachi.java.lucene.sample.entity.lucene.PostDocument;

import java.util.List;

public interface ForumCategoryParser {

    /**
     * Parse each forum category page,
     * load each post detail, including title, link,
     * post id, cdate, edate etc.
     * @param categoryUrl the url of category page.
     * @return returns list of format post info.
     * */
    List<PostDocument> process(String categoryUrl);
}
