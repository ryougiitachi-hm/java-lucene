package per.itachi.java.lucene.sample.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import per.itachi.java.lucene.sample.configuration.ForumCollectionProperties;

@RunWith(MockitoJUnitRunner.class)
public class JacksonYamlReaderTest {

    @InjectMocks
    private JacksonYamlReader reader;

    @Before
    public void setup() {
        ObjectMapper objectMapperYaml = new ObjectMapper(new YAMLFactory());
        ReflectionTestUtils.setField(reader, "objectMapperYaml", objectMapperYaml);
    }

    @Test
    public void readFromResource() {
        ForumCollectionProperties properties = reader.readFromResource("cfg-common.yaml");
        System.out.printf("properties=%s %n", properties);
    }
}
