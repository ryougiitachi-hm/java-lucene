package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.util.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class JdkHtmlDownloader implements HtmlDownloader {

    @Autowired
    private ConfigurationContext context;

    @Override
    public void download(String toDownloadLink, Path outputPath, ForumProperties properties) {
        // anti-prohibit
        sleepForAntiProhibit(properties.getIntervalFixed(), properties.getIntervalFlexible());

        // process
        try {
            log.info("[Downloader] Downloading {}. ", toDownloadLink);
            URL url = new URL(toDownloadLink);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
            fillRequestHeaders(connection, url, properties.getHeaders());// request headers
            connection.connect();
            // response
            handleResponseCode(connection.getResponseCode());
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
            // download file
            try(InputStream bis = new BufferedInputStream(url.openStream(),
                    context.getBufferReaderSize());
                OutputStream bos = new BufferedOutputStream(Files.newOutputStream(outputPath),
                        context.getBufferWriterSize())) {
                byte[] bytesData = new byte[Constants.DEFAULT_BUFFER_BYTE_SIZE];
                int count = 0;
                while ((count = bis.read(bytesData)) > 0) {
                    bos.write(bytesData, 0, count);
                }
            }
            finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            // handle post-download
            handleCompression(responseHeaders, outputPath);
            log.info("[Downloader] Downloaded {}. ", toDownloadLink);
        }
        catch (MalformedURLException e) {
            log.error("[Downloader] Error occurred when downloading {}. ", toDownloadLink, e);
        }
        catch (IOException e) {
            log.error("[Downloader] Error occurred when downloading and opening stream {}. ", toDownloadLink, e);
        }
    }

    private void sleepForAntiProhibit(int fixed, int flexible) {
        try {
            long lInterval =  fixed + ThreadLocalRandom.current().nextInt(flexible);
            log.debug("[Downloader] The current thread will sleep {} milliseconds.", lInterval);
            Thread.sleep(lInterval);
        }
        catch (InterruptedException e) {
            log.error("[Downloader] Interrupted when anti-killing for downloading html. ", e);
        }
    }

    private void fillRequestHeaders(HttpURLConnection connection, URL url, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setRequestProperty(Constants.HTTP_HEADER_HOST, url.getHost());
        connection.setRequestProperty(Constants.HTTP_HEADER_REFERER, url.toString());
    }

    private void handleResponseCode(int responseCode) {
        // TODO: handleResponseCode
    }

    private void handleCompression(Map<String, List<String>> responseHeaders, Path outputPath) throws IOException {
        List<String> contentEncodings = responseHeaders.get("Content-Encoding");
        if (CollectionUtils.isEmpty(contentEncodings)) {
            return;
        }
        log.debug("[Downloader] Content-Encoding is {}. ", contentEncodings);
        Path compressedPath = Paths.get(String.join(".", outputPath.toString(), contentEncodings.get(0)));
        if (Files.exists(compressedPath)) {
            Files.delete(compressedPath);
        }
        Files.copy(outputPath, compressedPath);
        try(InputStream bis = new BufferedInputStream(new GZIPInputStream(Files.newInputStream(compressedPath)));
            OutputStream bos = new BufferedOutputStream(Files.newOutputStream(outputPath));) {
            byte[] bytesData = new byte[Constants.DEFAULT_BUFFER_BYTE_SIZE];
            int count = 0;
            while ((count = bis.read(bytesData)) > 0) {
                bos.write(bytesData, 0, count);
            }
        }
    }

}
