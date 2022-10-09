package per.itachi.java.lucene.practice.infra.restful.downloader;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public interface RestfulDownloader {

    File downloadAsFile(String url, Map<String, String> headers, Path outputPath);

    InputStream downloadAsStream(String url, Map<String, String> headers);
}