package per.itachi.java.lucene.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;
import per.itachi.java.lucene.sample.entity.lucene.PostDocument;
import per.itachi.java.lucene.sample.parser.ForumCategoryParser;
import per.itachi.java.lucene.sample.persist.CommonIndexManager;
import per.itachi.java.lucene.sample.service.IndicesService;
import per.itachi.java.lucene.sample.util.CommonUtils;

import java.util.List;

@Slf4j
@Service
public class IndicesServiceImpl implements IndicesService {

    @Autowired
    private ConfigurationContext context;

    @Autowired
    private ForumCategoryParser parser;

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
        List<PostDocument> postDocumentList = parser.process(url);
        log.info("Finished parsing url {}. ", url);
        log.info("Start writing indices. ");
        indexManager.writeIndices(properties.getName(), postDocumentList);
        log.info("Finished writing indices. ");
    }
}
