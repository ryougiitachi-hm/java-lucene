package per.itachi.java.lucene.practice.infra.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import per.itachi.java.lucene.practice.infra.lucene.config.LuceneProperties;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = IndexManagerImplTest.TestConfiguration.class)
public class IndexManagerImplTest {

    @Autowired
    private LuceneProperties luceneProperties;

    @Autowired
    private IndexManagerImpl indexManager;

    @Autowired
    private Environment environment;

    @Value("${infra.lucene.base-path}")
    private String indexPath; // unable to set

//    @Ignore
    @Test
    public void locateLeafReader() {
//        String basePath = environment.getProperty("infra.lucene.base-path"); // don't know why
        String indicesBasePath = System.getenv("INDICES_BASE_PATH");
        String indicesName = System.getenv("INDICES_NAME");
        Path pathIdx = Paths.get(indicesBasePath, indicesName);
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(pathIdx))) {
//            reader.numDocs();
            int maxDoc = reader.maxDoc();
            List<LeafReaderContext> leafReaderContextList = reader.leaves();
            for (int docID = 0; docID < maxDoc; ++docID) {
                int location = indexManager.locateLeafReader(leafReaderContextList, docID);
                log.info("Verifying, docID={}, leaf-location={}, leaf-docBase={}, leaf-numDocs={}. ",
                        docID, location, leafReaderContextList.get(location).docBase, leafReaderContextList.get(location).reader().numDocs());
                boolean judgement = location < leafReaderContextList.size() - 1
                        ? leafReaderContextList.get(location).docBase <= docID && docID < leafReaderContextList.get(location + 1).docBase
                        : leafReaderContextList.get(location).docBase <= docID;
                assertTrue(judgement);
            }
        }
        catch (IOException e) {
            log.error("Error occurred. ", e);
        }
    }

    @Ignore
    @Test
    public void locateLeafReader_docID() {
        String indicesBasePath = System.getenv("INDICES_BASE_PATH");
        String indicesName = System.getenv("INDICES_NAME");
        Path pathIdx = Paths.get(indicesBasePath, indicesName);
        int docID = 18655;
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(pathIdx))) {
            List<LeafReaderContext> leafReaderContextList = reader.leaves();
            indexManager.locateLeafReader(leafReaderContextList, docID); // to debug
        }
        catch (IOException e) {
            log.error("Error occurred. ", e);
        }
    }

    @Configuration
    @PropertySource({"application.yaml", "application-dev.yaml"}) // not effective, it doesn't work
    @ComponentScan("per.itachi.java.lucene.practice.infra.lucene")
    static class TestConfiguration {
    }
}
