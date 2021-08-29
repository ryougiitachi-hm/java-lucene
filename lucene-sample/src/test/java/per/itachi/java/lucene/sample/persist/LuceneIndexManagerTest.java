package per.itachi.java.lucene.sample.persist;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.entity.lucene.PostDocument;

import java.time.OffsetDateTime;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class LuceneIndexManagerTest {

    @InjectMocks
    private LuceneIndexManager manager;

    @Before
    public void setup() {
        ConfigurationContext configurationContext = new ConfigurationContext();
        configurationContext.setIndexDirectory("indices");
        ReflectionTestUtils.setField(manager, "configurationContext", configurationContext);
    }

    @Test
    public void writeIndices_list_return() {
        PostDocument document = new PostDocument();
        document.setPostId(11469259L);
        document.setFileName("cnblogs-11469259-Lucene-staring-guide.html");
        document.setFilePath("html/cnblogs-11469259-Lucene-staring-guide.html");
        document.setCdate(OffsetDateTime.now());
        document.setEdate(OffsetDateTime.now());
        manager.writeIndices("cnblogs", Collections.singletonList(document));
    }
}
