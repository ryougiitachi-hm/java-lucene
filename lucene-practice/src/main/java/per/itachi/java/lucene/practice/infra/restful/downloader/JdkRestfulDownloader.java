package per.itachi.java.lucene.practice.infra.restful.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.practice.common.exception.CommonBusinessException;
import per.itachi.java.lucene.practice.infra.restful.HttpConstants;

@Slf4j
@Component
public class JdkRestfulDownloader implements RestfulDownloader{

    @Override
    public File downloadAsFile(String url, Map<String, String> headers, Path outputPath) {
        try(InputStream is = downloadAsStream(url, headers);
            OutputStream fos = new FileOutputStream(outputPath.toFile())) {
            byte[] bytes = new byte[HttpConstants.DEFAULT_BUFFER_BYTE_SIZE];
            int countOfBytes = 0;
            while ((countOfBytes = is.read(bytes)) > 0) {
                fos.write(bytes, 0,countOfBytes);
            }
            return outputPath.toFile();
        }
        catch (IOException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }

    @Override
    public InputStream downloadAsStream(String url, Map<String, String> headers) {
        return null;
    }
}
