package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.CategoryInfo;
import per.itachi.java.lucene.sample.entity.html.PostInfo;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;
import per.itachi.java.lucene.sample.util.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    public CategoryInfo parseCategory(Path htmlPath, UrlInfo urlInfo, ForumProperties properties) {

        log.info("[Parser] Parsing html category {}. ", htmlPath);

        // fid
        String strFid = "";
        if (!CollectionUtils.isEmpty(urlInfo.getParams()) 
        		&& !CollectionUtils.isEmpty(properties.getCategoryParams())) {
        	strFid = urlInfo.getParams().get(properties.getCategoryParams().get(0));
		}
        
        try(InputStream bis = new BufferedInputStream(Files.newInputStream(htmlPath),
                context.getBufferReaderSize())) {
            String nextPageUrl;
            List<PostInfo> postInfoList = new LinkedList<>();

            Document document = Jsoup.parse(bis, properties.getCharset(), urlInfo.getBaseRelativeUri());
            Elements elementsPostList = document.select(properties.getCategoryPostListSelector());
//            int countCategoryLine = 0;// count of current category lines
            for (Element post : elementsPostList) {
//                ++countCategoryLine;
                Element elementTitle = post.selectFirst(properties.getCategoryPostInlineTitleSelector());
                Element elementAddressUrl = post.selectFirst(properties.getCategoryPostInlineUrlSelector());
                Element elementCdate = post.selectFirst(properties.getCategoryPostInlineCdateSelector());
                Element elementEdate = post.selectFirst(properties.getCategoryPostInlineEdateSelector());
                PostInfo postInfo = new PostInfo();
                postInfo.setCategoryId(Long.parseLong(strFid));
                postInfo.setTitle(Objects.nonNull(elementTitle) ? elementTitle.text() : "");
                postInfo.setAddressLink(Objects.nonNull(elementAddressUrl)
                        ? completeUrl(elementAddressUrl.attr(Constants.HTML_ATTR_A_HREF), urlInfo) : "");
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
                nextPageUrl = completeUrl(elementNextPage.attr(Constants.HTML_ATTR_A_HREF), urlInfo);
            }
            log.info("[Parser] Parsed html category {}. ", htmlPath);
            
            // CategoryInfo
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setFid(strFid);
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
    public PostInfo parsePost(Path htmlPath, ForumProperties properties) {
        log.info("[Parser] Parsing html post {}. ", htmlPath);
        try(InputStream bis = new BufferedInputStream(Files.newInputStream(htmlPath),
                context.getBufferReaderSize())) {

            Document document = Jsoup.parse(bis, properties.getCharset(), ""); // TODO: relative links URL.
            Element elementTitle = document.selectFirst(properties.getPostTitleSelector());
            String strTitle = Objects.nonNull(elementTitle) ? elementTitle.text() : "";
            log.info("[Parser] Parsed html category {}. ", htmlPath);
            
            PostInfo postInfo = new PostInfo();
            postInfo.setTitle(strTitle);
            return postInfo;
		} 
        catch (IOException e) {
            log.error("[Parser] Failed to open and parse post html file {}. ", htmlPath, e);
            return null;
		}
    }

    private String completeUrl(String addressLink, UrlInfo urlInfo) {
        try {
            new URL(addressLink);// check whether it is url or not.
            return addressLink;
        }
        catch (MalformedURLException e) {
            if (addressLink.startsWith("/")) { // absolute path
                if (addressLink.length() >= 2) {
                    return urlInfo.getBaseUri() + addressLink;
                }
                else {
                    return urlInfo.getBaseUri();
                }
            }
            else { // relative path
                return String.join("/", urlInfo.getBaseRelativeUri(), addressLink);
            }
        }
    }
}
