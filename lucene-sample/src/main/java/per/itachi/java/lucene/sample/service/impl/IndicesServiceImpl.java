package per.itachi.java.lucene.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
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
import per.itachi.java.lucene.sample.parser.ForumParser;
import per.itachi.java.lucene.sample.parser.HtmlDownloader;
import per.itachi.java.lucene.sample.persist.CommonIndexManager;
import per.itachi.java.lucene.sample.service.IndicesService;
import per.itachi.java.lucene.sample.util.CommonUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The structure of html folder: [html]/[index-name]/category.
 * */
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
            Path outputPath = Paths.get(context.getHtmlDirectory(), properties.getName(), context.getCategoryDirectory(),
                    CommonUtils.generateHtmlFileName(nextPageUrlInfo, properties.getCategoryParams()));
            downloader.download(nextPageUrl, outputPath, properties);
            CategoryInfo categoryInfo = parser.parseCategory(outputPath, urlInfo, properties);
            if (Objects.isNull(categoryInfo)) {
                nextPageUrl = null;
            }
            else {
                categoryInfoList.add(categoryInfo);
                nextPageUrl = categoryInfo.getNextPageUrl();
            }
        }
        log.info("Finished downloading and parsing forum [{}] {} categories. ", properties.getName(), countCategories);

        List<PostDocument> postDocumentList = processPostsFrom(categoryInfoList, properties);
        log.info("Finished parsing url {}. ", url);
        
        log.info("Start writing indices. ");
        indexManager.writeIndices(properties.getName(), postDocumentList);
        log.info("Finished writing indices. ");
    }

    @Override
    public void updateIndicesFromCategoryFolder(String indexName) {
        ForumProperties properties = context.getForumPropertiesByName(indexName);
        if (properties == null) {
            log.error("The index name {} is not configured. ", indexName);
            return;
        }

        Path categoryHtmlPath = Paths.get(context.getHtmlDirectory(), properties.getName(), context.getCategoryDirectory());
        log.info("Checking existence of html category directory {}. ", categoryHtmlPath);
        if (!Files.exists(categoryHtmlPath)) {
            log.error("The html category directory {} doesn't exist! ", categoryHtmlPath);
            return;
        }

        String strExampleCategoryUrl = String.format("http://%s/forum/a", properties.getDomains().get(0));
        UrlInfo urlInfo = CommonUtils.generateUrlInfo(strExampleCategoryUrl);
        if (!StringUtils.hasText(urlInfo.getHost())) {
            log.error("The url {} is not valid address. ", strExampleCategoryUrl);
            return;
        }

        log.info("Start processing forum [{}] categories. ", properties.getName());
        List<CategoryInfo> categoryInfoList = new LinkedList<>();
        int countCategories = 0;
        try(DirectoryStream<Path> iteratorCategoriesHtml = Files
                .newDirectoryStream(categoryHtmlPath, entry -> entry.toString().endsWith(".html"))) {
            for (Path categoriesHtmlPath : iteratorCategoriesHtml) {
                CategoryInfo categoryInfo = parser.parseCategory(categoriesHtmlPath, urlInfo, properties);
                categoryInfoList.add(categoryInfo);
                ++countCategories;
            }
        }
        catch (IOException e) {
            log.error("Error occurred when traversing {}. ", categoryHtmlPath, e);
        }
        log.info("Finished processing forum [{}] {} categories. ", properties.getName(), countCategories);

        List<PostDocument> postDocumentList = processPostsFrom(categoryInfoList, properties);
        log.info("Finished parsing url {}. ", urlInfo);

        log.info("Start writing indices. ");
        indexManager.writeIndices(properties.getName(), postDocumentList);
        log.info("Finished writing indices. ");
    }

    private List<PostDocument> processPostsFrom(List<CategoryInfo> categoryInfoList, ForumProperties properties) {
        log.info("Start downloading forum [{}] posts from categories. ", properties.getName());
        // download and wrap post list.
        // not so good, to decouple PostInfo and PostDocument.
        int countPages = 0;
        int countPosts = 0;
        List<PostDocument> postDocumentList = new LinkedList<>();
        for (CategoryInfo categoryInfo : categoryInfoList) {
            ++ countPages;
            int countPostsPerCategory = 0;
            for (PostInfo postInfo : categoryInfo.getPostInfos()) {
                ++ countPosts;
                ++ countPostsPerCategory;
                log.info("[Service] Start processing category {} post {} total {}. ",
                        countPages, countPostsPerCategory, countPosts);
                UrlInfo postUrlInfo = CommonUtils.generateUrlInfo(postInfo.getAddressLink());
                Path outputPath = Paths.get(context.getHtmlDirectory(), properties.getName(), context.getPostDirectory(),
                        CommonUtils.generateHtmlFileName(postUrlInfo, properties.getPostParams()));
                downloader.download(postInfo.getAddressLink(), outputPath, properties);
                PostDocument postDocument = new PostDocument();
                postDocument.setPostId(generatePostId(postUrlInfo));
                postDocument.setFilePath(outputPath.toString());
                postDocument.setFileName(outputPath.getFileName().toString());
                postDocument.setTitle(postInfo.getTitle());
                postDocumentList.add(postDocument);
            }
        }
        log.info("Finished downloading forum [{}] {} posts. ", properties.getName(), countPosts);
        return postDocumentList;
    }

    @Override
    public void updateIndicesFromPostFolder(String indexName) {
        ForumProperties properties = context.getForumPropertiesByName(indexName);
        if (properties == null) {
            log.error("The index name {} is not configured. ", indexName);
            return;
        }

        Path postHtmlPath = Paths.get(context.getHtmlDirectory(), properties.getName(), context.getPostDirectory());
        log.info("Checking existence of html post directory {}. ", postHtmlPath);
        if (!Files.exists(postHtmlPath)) {
            log.error("The html post directory {} doesn't exist! ", postHtmlPath);
            return;
        }

        log.info("Start processing forum [{}] posts. ", properties.getName());
        List<PostDocument> postDocumentList = new LinkedList<>();
        int countPosts = 0;
        try(DirectoryStream<Path> iteratorPostsHtml = Files
                .newDirectoryStream(postHtmlPath, entry -> entry.toString().endsWith(".html"))) {
            for (Path postFilePath : iteratorPostsHtml) {
                PostDocument postDocument = new PostDocument();
                postDocument.setPostId(generatePostId(postFilePath.getFileName().toString(), properties.getPostParams()));
                postDocument.setFilePath(postFilePath.toString());
                postDocument.setFileName(postFilePath.getFileName().toString());
                postDocument.setTitle(postFilePath.getFileName().toString());
                postDocumentList.add(postDocument);
                ++countPosts;
                log.info("Processing post file {} {}. ", postFilePath, countPosts);
            }
        }
        catch (IOException e) {
            log.error("Error occurred when traversing {}. ", postHtmlPath, e);
        }
        log.info("Finished processing forum [{}] {} posts. ", properties.getName(), countPosts);

        log.info("Start writing indices. ");
        indexManager.writeIndices(properties.getName(), postDocumentList);
        log.info("Finished writing indices. ");
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

    private long generatePostId(String fileName, List<String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return 0;
        }
        int idxTag = fileName.indexOf(params.get(0));
        int idxSplitter = fileName.indexOf(".", idxTag);
        String strPostId = fileName.substring(idxTag + params.get(0).length(), idxSplitter);
        return Long.valueOf(strPostId);
    }

}
