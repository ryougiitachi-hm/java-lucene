package per.itachi.java.lucene;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE;

public class CreateApplication {

    private static final Logger logger = LoggerFactory.getLogger(CreateApplication.class);

    /**
     * @param args
     *          0 - doc path
     *          1 - index path
     * */
    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("the length of arguments shouldn't be less than 1. ");
            return;
        }

        String strDocPath = args[0];
        String strIdxPath = "indices";

        try {
            Directory dirIdx = FSDirectory.open(Paths.get(strIdxPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
            writerConfig.setOpenMode(CREATE);
            try(IndexWriter writer = new IndexWriter(dirIdx, writerConfig);) {
                if (!Files.isRegularFile(Paths.get(strDocPath))) {
                    logger.error("Must be a regular file. ");
                    return; // currently support only regular files.
                }
                try(InputStream is = Files.newInputStream(Paths.get(strDocPath))) {
                    logger.info("Indexing file {} into specific path. ", strDocPath);
                    Document document = new Document();
                    document.add(new StringField("file_name", Paths.get(strDocPath).toString(), Field.Store.YES));
                    document.add(new TextField("content",
                            new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))));
                    writer.addDocument(document);
                }
            }
        }
        catch (IOException e) {
            logger.error("", e);
        }

    }
}
