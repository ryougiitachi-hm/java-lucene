package per.itachi.java.lucene.sample.component;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import per.itachi.java.lucene.sample.configuration.ForumCollectionProperties;

@RunWith(MockitoJUnitRunner.class)
public class SnakeYamlReaderTest {

    @InjectMocks
    private SnakeYamlReader reader;

    @Test
    public void readFromResource_shouldReturn() {
        // Error occured when using /cfg-common.yaml ...
        ForumCollectionProperties properties = reader.readFromResource("cfg-common.yaml");
        System.out.printf("properties=%s %n", properties);
    }
}
