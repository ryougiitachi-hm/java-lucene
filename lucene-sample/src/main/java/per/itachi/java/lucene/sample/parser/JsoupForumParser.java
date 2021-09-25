package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.CategoryInfo;
import per.itachi.java.lucene.sample.entity.html.PostInfo;
import per.itachi.java.lucene.sample.util.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JsoupForumParser implements ForumParser {

    @Autowired
    private ConfigurationContext context;

    @Override
    public CategoryInfo parseCategory(Path htmlPath, String baseUri, ForumProperties properties) {

        try(InputStream bis = new BufferedInputStream(Files.newInputStream(htmlPath),
                context.getBufferReaderSize())) {
            String nextPageUrl;
            List<PostInfo> postInfoList = new LinkedList<>();

            Document document = Jsoup.parse(bis, properties.getCharset(), baseUri);
            Elements elementPostList = document.select(properties.getCategoryPostListSelector());
            int countCategoryLine = 0;// count of current category lines
            for (Element post : elementPostList) {
                ++countCategoryLine;
                Element elementTitle = post.selectFirst(properties.getCategoryPostInlineTitleSelector());
                Element elementAddressUrl = post.selectFirst(properties.getCategoryPostInlineUrlSelector());
                Element elementCdate = post.selectFirst(properties.getCategoryPostInlineCdateSelector());
                Element elementEdate = post.selectFirst(properties.getCategoryPostInlineEdateSelector());
                PostInfo postInfo = new PostInfo();
                postInfo.setTitle(Objects.nonNull(elementTitle) ? elementTitle.text() : "");
                postInfo.setAddressLink(Objects.nonNull(elementAddressUrl)
                        ? elementAddressUrl.attr(Constants.HTML_ATTR_A_HREF) : "");
                postInfo.setCdate(Objects.nonNull(elementCdate) ? elementCdate.text() : "");
                postInfo.setEdate(Objects.nonNull(elementEdate) ? elementEdate.text() : "");
                postInfoList.add(postInfo);
            }
            // nextPageUrl
            Element elementNextPage = document.selectFirst(properties.getCategoryNextPageSelector());
            if (Objects.isNull(elementNextPage)) {
                nextPageUrl = null;
            }
            else {
                nextPageUrl = elementNextPage.attr(Constants.HTML_ATTR_A_HREF);
            }

            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setPostInfos(postInfoList);
            categoryInfo.setNextPageUrl(nextPageUrl);
            return categoryInfo;
        }
        catch (IOException e) {
            log.error("[Parser] Failed to open and parse category html file {}. ", htmlPath, e);
            return null;
        }
    }

    @Override
    public void parsePost(Path htmlPath) {
    }
}
