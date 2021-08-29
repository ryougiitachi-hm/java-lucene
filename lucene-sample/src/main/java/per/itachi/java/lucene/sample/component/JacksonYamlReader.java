package per.itachi.java.lucene.sample.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.sample.configuration.ForumCollectionProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JacksonYamlReader implements YamlReader {

    private final Logger logger = LoggerFactory.getLogger(JacksonYamlReader.class);

    @Autowired
    private ObjectMapper objectMapperYaml;

    @Override
    public ForumCollectionProperties readFromResource(String resource) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            ForumCollectionProperties properties = objectMapperYaml.readValue(br, ForumCollectionProperties.class);
            return properties;
        }
        catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }
}
