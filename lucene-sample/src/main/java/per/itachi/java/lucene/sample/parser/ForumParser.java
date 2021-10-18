package per.itachi.java.lucene.sample.parser;

import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.CategoryInfo;
import per.itachi.java.lucene.sample.entity.html.PostInfo;
import per.itachi.java.lucene.sample.entity.html.UrlInfo;

import java.nio.file.Path;

public interface ForumParser {

    CategoryInfo parseCategory(Path htmlPath, UrlInfo urlInfo, ForumProperties properties);

    PostInfo parsePost(Path htmlPath, ForumProperties properties);
}
