package per.itachi.java.lucene.sample.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import per.itachi.java.lucene.sample.configuration.ForumCollectionProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class SnakeYamlReader implements YamlReader {

    private final Logger logger = LoggerFactory.getLogger(SnakeYamlReader.class);

    @Override
    public ForumCollectionProperties readFromResource(String resource) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
            Yaml yaml = new Yaml();
            ForumCollectionProperties properties = yaml.loadAs(bufferedReader, ForumCollectionProperties.class);
            return properties;
        }
        catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }
}