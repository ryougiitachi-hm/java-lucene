package per.itachi.java.lucene.sample.parser;

import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.CategoryInfo;

import java.nio.file.Path;

public interface ForumParser {

    CategoryInfo parseCategory(Path htmlPath, String baseUri, ForumProperties properties);

    void parsePost(Path htmlPath);
}
