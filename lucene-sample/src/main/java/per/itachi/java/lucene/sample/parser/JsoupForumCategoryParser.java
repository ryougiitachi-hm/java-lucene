package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.PostInfo;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;
import per.itachi.java.lucene.sample.entity.lucene.PostDocument;
import per.itachi.java.lucene.sample.util.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
public class JsoupForumCategoryParser implements ForumCategoryParser {

    @Autowired
    private ConfigurationContext context;

    @Autowired
    private HtmlDownloader downloader;

    @Override
    public List<PostDocument> process(String categoryUrl) {
        ForumProperties forumProperties = getForumPropertiesByUrl(categoryUrl);
        if (Objects.isNull(forumProperties)) {
            log.error("No matched configuration found for {}", categoryUrl);
            return Collections.emptyList();
        }

        // check existence of html directory
        Path pathHtml = Paths.get(context.getHtmlDirectory(), forumProperties.getName());
        log.info("Checking existence of html directory {}. ", pathHtml);
        if (!Files.exists(pathHtml)) {
            try {
                log.info("The html {} doesn't exist, creating...", pathHtml);
                Files.createDirectories(pathHtml);
                log.info("Created {}.", pathHtml);
            }
            catch (IOException e) {
                log.error("Failed to create dir {} when checking existence of html directory. ",
                        pathHtml, e);
                return Collections.emptyList();
            }
        }

        log.info("Start downloading and parsing forum [{}] categories. ", forumProperties.getName());
        List<PostInfo> postInfoList = new LinkedList<>();
        int countCategories = 0;
        String nextPageUrl = categoryUrl;
        while (StringUtils.hasText(nextPageUrl)) {
            UrlInfo urlInfo = generateUrlInfo(nextPageUrl);
            if (!StringUtils.hasText(urlInfo.getHost())) {
                break;
            }

            ++countCategories;
            Path outputPath = Paths.get(context.getHtmlDirectory(), forumProperties.getName(),
                    generateHtmlFileName(urlInfo, forumProperties.getCategoryParams()));
            downloader.download(nextPageUrl, outputPath, forumProperties);
            try(InputStream bis = new BufferedInputStream(Files.newInputStream(outputPath),
                    context.getBufferReaderSize())) {
                Document document = Jsoup.parse(bis, forumProperties.getCharset(), urlInfo.getBaseUri());
                Elements elementPostList = document.select(forumProperties.getCategoryPostListSelector());
                int countCategoryLine = 0;// count of current category lines
                for (Element post : elementPostList) {
                    ++countCategoryLine;
                    Element elementTitle = post.selectFirst(forumProperties.getCategoryPostInlineTitleSelector());
                    Element elementAddressUrl = post.selectFirst(forumProperties.getCategoryPostInlineUrlSelector());
                    Element elementCdate = post.selectFirst(forumProperties.getCategoryPostInlineCdateSelector());
                    Element elementEdate = post.selectFirst(forumProperties.getCategoryPostInlineEdateSelector());
                    PostInfo postInfo = new PostInfo();
                    postInfo.setTitle(Objects.nonNull(elementTitle) ? elementTitle.text() : "");
                    postInfo.setAddressLink(Objects.nonNull(elementAddressUrl)
                            ? elementAddressUrl.attr(Constants.HTML_ATTR_A_HREF) : "");
                    postInfo.setCdate(Objects.nonNull(elementCdate) ? elementCdate.text() : "");
                    postInfo.setEdate(Objects.nonNull(elementEdate) ? elementEdate.text() : "");
                    postInfoList.add(postInfo);
                }
                // nextPageUrl
                Element elementNextPage = document.selectFirst(forumProperties.getCategoryNextPageSelector());
                if (Objects.isNull(elementNextPage)) {
                    nextPageUrl = null;
                }
                else {
                    nextPageUrl = elementNextPage.attr(Constants.HTML_ATTR_A_HREF);
                }
            }
            catch (IOException e) {
                log.error("[Parser] Failed to open and parse category html file {}. ", outputPath, e);
            }
        }
        log.info("Finished downloading and parsing forum [{}] {} categories. ", forumProperties.getName(), countCategories);

        log.info("Start downloading forum [{}] {} posts. ", forumProperties.getName(), postInfoList.size());
        // download and wrap post list.
        // not so good, to decouple PostInfo and PostDocument.
        List<PostDocument> postDocumentList = new LinkedList<>();
        for (PostInfo postInfo : postInfoList) {
            UrlInfo urlInfo = generateUrlInfo(postInfo.getAddressLink());
            Path outputPath = Paths.get(context.getHtmlDirectory(), forumProperties.getName(),
                    generateHtmlFileName(urlInfo, forumProperties.getPostParams()));
            downloader.download(postInfo.getAddressLink(), outputPath, forumProperties);
            PostDocument postDocument = new PostDocument();
            postDocument.setPostId(generatePostId(urlInfo));
            postDocument.setFilePath(outputPath.toString());
            postDocument.setTitle(postInfo.getTitle());
            postDocumentList.add(postDocument);
        }
        log.info("Finished downloading forum [{}] {} posts. ", forumProperties.getName(), postInfoList.size());

        return postDocumentList;
    }

    private ForumProperties getForumPropertiesByUrl(String categoryUrl) {
        try {
            URL url = new URL(categoryUrl);
            String host = url.getHost();
            ForumProperties forumProperties = context.getForumPropertiesByDomain(host);
            if (!Objects.isNull(forumProperties)) {
                log.info("[Parser] Matched forum {} for {}. ", forumProperties.getName(), categoryUrl);
            }
            return forumProperties;
        }
        catch (MalformedURLException e) {
            log.error("Failed to malformed url {}. ", categoryUrl, e);
            return null;
        }
    }

    private UrlInfo generateUrlInfo(String addressUrl) {
        try {
            URL url = new URL(addressUrl);
            // file name
            String strPaths = url.getPath();
            String strFileName = null;
            if (StringUtils.hasText(strPaths)) {
                strFileName = strPaths.substring(strPaths.lastIndexOf("/") + 1);
            }
            // paths
            List<String> paths = Collections.emptyList();
            if (StringUtils.hasText(strPaths) && strPaths.length() > 1) {
                paths = Arrays.asList(strPaths.split("/"));
            }
            // query
            String strQuery = url.getQuery();
            Map<String, String> mapParams = Collections.emptyMap();
            if (StringUtils.hasText(strQuery)) {
                mapParams = new HashMap<>();
                String[] arrParams = strQuery.split("&");
                for (String param : arrParams) {
                    int idx = param.indexOf("=");
                    mapParams.put(param.substring(0, idx), param.substring(idx + 1));
                }
            }
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setHost(url.getHost());
            urlInfo.setBaseUri(url.getPort() <= 0 ? String.format("%s://%s", url.getProtocol(), url.getHost())
                    : String.format("%s://%s:%d", url.getProtocol(), url.getHost(), url.getPort()));
            urlInfo.setPaths(paths);
            urlInfo.setFileName(strFileName);
            urlInfo.setParams(mapParams);
            return urlInfo;
        }
        catch (MalformedURLException e) {
            log.error("[Parser] Failed to generate url info, url={}. ", addressUrl, e);
            return new UrlInfo();
        }
    }

    private String generateHtmlFileName(UrlInfo urlInfo, List<String> params) {
        if (CollectionUtils.isEmpty(urlInfo.getPaths()) && CollectionUtils.isEmpty(urlInfo.getParams())) {
            return UUID.randomUUID().toString();
        }
        String strLastPath = urlInfo.getPaths().get(urlInfo.getPaths().size() - 1);
        int idx = strLastPath.indexOf(".");
        StringBuilder builder = new StringBuilder(100);
        // path part
        if (idx > 0) {
            builder.append(strLastPath.substring(0, idx));
        }
        else {
            builder.append(strLastPath);
        }
        // param path
        for (String param : params) {
            builder.append("-").append(param).append(urlInfo.getParams().get(param));
        }
        builder.append(".html");
        return builder.toString();
    }

    private long generatePostId(UrlInfo urlInfo) {
        if (CollectionUtils.isEmpty(urlInfo.getParams())) {
            if (CollectionUtils.isEmpty(urlInfo.getPaths())) {
                return 0;
            }
            else {
                return Long.parseLong(urlInfo.getPaths().get(urlInfo.getPaths().size() - 1));
            }
        }
        Map<String, String> params = urlInfo.getParams();
        if (params.get("tid") == null) {
            return 0;
        }
        else {
            return Long.parseLong(params.get("tid"));
        }
    }
}
