package per.itachi.java.lucene.sample.parser;

import per.itachi.java.lucene.sample.configuration.ForumProperties;

import java.nio.file.Path;

public interface HtmlDownloader {

    void download(String url, Path outputPath, ForumProperties properties);
}
