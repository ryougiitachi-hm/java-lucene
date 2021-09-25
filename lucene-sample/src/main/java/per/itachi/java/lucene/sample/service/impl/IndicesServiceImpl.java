package per.itachi.java.lucene.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.CategoryInfo;
import per.itachi.java.lucene.sample.entity.html.PostInfo;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;
import per.itachi.java.lucene.sample.entity.lucene.PostDocument;
import per.itachi.java.lucene.sample.parser.ForumCategoryParser;
import per.itachi.java.lucene.sample.parser.ForumParser;
import per.itachi.java.lucene.sample.parser.HtmlDownloader;
import per.itachi.java.lucene.sample.persist.CommonIndexManager;
import per.itachi.java.lucene.sample.service.IndicesService;
import per.itachi.java.lucene.sample.util.CommonUtils;
import per.itachi.java.lucene.sample.util.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class IndicesServiceImpl implements IndicesService {

    @Autowired
    private ConfigurationContext context;

    @Autowired
    private ForumParser parser;
    
    @Autowired
    private HtmlDownloader downloader;

    @Autowired
    private CommonIndexManager indexManager;

    @Override
    public void updateIndices(String url) {
        UrlInfo urlInfo = CommonUtils.generateUrlInfo(url);
        if (!StringUtils.hasText(urlInfo.getHost())) {
            log.error("The url {} is not valid address. ", url);
            return;
        }

        ForumProperties properties = context.getForumPropertiesByDomain(urlInfo.getHost());
        if (properties == null) {
            log.error("The domain url {} is not configured. ", urlInfo.getHost());
            return;
        }

        log.info("Start parsing url {}. ", url);

        // check existence of html directory
        Path pathHtml = Paths.get(context.getHtmlDirectory(), properties.getName());
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
                return;
            }
        }

        log.info("Start downloading and parsing forum [{}] categories. ", properties.getName());
        List<CategoryInfo> categoryInfoList = new LinkedList<>();
        int countCategories = 0;
        String nextPageUrl = url;
        while (StringUtils.hasText(nextPageUrl)) {
            UrlInfo nextPageUrlInfo = CommonUtils.generateUrlInfo(nextPageUrl);
            if (!StringUtils.hasText(nextPageUrlInfo.getHost())) {
                break;
            }

            ++countCategories;
            Path outputPath = Paths.get(context.getHtmlDirectory(), properties.getName(),
                    generateHtmlFileName(nextPageUrlInfo, properties.getCategoryParams()));
            downloader.download(nextPageUrl, outputPath, properties);
            CategoryInfo categoryInfo = parser.parseCategory(outputPath, nextPageUrlInfo.getBaseUri(), properties);
            if (Objects.isNull(categoryInfo)) {
                nextPageUrl = null;
            }
            else {
                categoryInfoList.add(categoryInfo);
                nextPageUrl = categoryInfo.getNextPageUrl();
            }
        }
        log.info("Finished downloading and parsing forum [{}] {} categories. ", properties.getName(), countCategories);

        log.info("Start downloading forum [{}] posts from categories. ", properties.getName());
        // download and wrap post list.
        // not so good, to decouple PostInfo and PostDocument.
        int countPosts = 0;
        List<PostDocument> postDocumentList = new LinkedList<>();
        for (CategoryInfo categoryInfo : categoryInfoList) {
            for (PostInfo postInfo : categoryInfo.getPostInfos()) {
                ++ countPosts;
                UrlInfo postUrlInfo = CommonUtils.generateUrlInfo(postInfo.getAddressLink());
                Path outputPath = Paths.get(context.getHtmlDirectory(), properties.getName(),
                        generateHtmlFileName(postUrlInfo, properties.getPostParams()));
                downloader.download(postInfo.getAddressLink(), outputPath, properties);
                PostDocument postDocument = new PostDocument();
                postDocument.setPostId(generatePostId(postUrlInfo));
                postDocument.setFilePath(outputPath.toString());
                postDocument.setTitle(postInfo.getTitle());
                postDocumentList.add(postDocument);
            }
        }
        log.info("Finished downloading forum [{}] {} posts. ", properties.getName(), countPosts);

        log.info("Finished parsing url {}. ", url);
        
        log.info("Start writing indices. ");
        indexManager.writeIndices(properties.getName(), postDocumentList);
        log.info("Finished writing indices. ");
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
