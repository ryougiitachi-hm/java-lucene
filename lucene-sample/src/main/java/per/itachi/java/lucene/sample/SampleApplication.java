package per.itachi.java.lucene.sample;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class SampleApplication {

    private final Logger logger = LoggerFactory.getLogger(SampleApplication.class);

    public static void main(String[] args) {

    }

    /**
     * @return list all of thread urls.
     * */
    private List<String> listAllThreadUrls() {
        return Collections.emptyList();
    }

    /**
     * @param inputDocumentPaths
     * @param outputIndicesPath
     * @since
     * */
    private void writeIndices(List<String> inputDocumentPaths, String outputIndicesPath) {
        if (CollectionUtils.isEmpty(inputDocumentPaths)) {
            // logger.error
            return;
        }
        if (!StringUtils.hasText(outputIndicesPath)) {
            // logger.error
            return;
        }
        // initialise config.
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // start writing indices
        try(Directory dirOutputIndices = FSDirectory.open(Paths.get(outputIndicesPath));
            IndexWriter writer = new IndexWriter(dirOutputIndices, config)) {
            logger.info("Start writing index into directory {}. ", dirOutputIndices);
            for (String inputDocumentPath : inputDocumentPaths) {
                try(InputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(inputDocumentPath)))) {
                    Document document = new Document();
                    document.add(new StringField("file_name",
                            Paths.get(inputDocumentPath).getFileName().toString(), Field.Store.YES));
                    document.add(new TextField("content",
                            new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8))));
                    writer.addDocument(document);
                }
            }
        }
        catch (IOException e) {
            logger.error("", e);
        }
    }
}
