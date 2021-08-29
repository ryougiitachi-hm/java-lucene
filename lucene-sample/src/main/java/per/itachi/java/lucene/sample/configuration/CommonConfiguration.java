package per.itachi.java.lucene.sample.configuration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import per.itachi.java.lucene.sample.component.YamlReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class CommonConfiguration {

    @Autowired
    private YamlReader reader;

    @Bean
    public ObjectMapper objectMapperYaml() {
        JsonFactory factory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(factory);
        return objectMapper;
    }

    @Bean("configurationContext")
    public ConfigurationContext loadForumProperties() {
        ForumCollectionProperties properties = reader.readFromResource("/cfg-common.yaml");
        if (Objects.isNull(properties)) {
            throw new RuntimeException("Failed to load yaml configuration. ");
        }

        Map<String, ForumProperties> forumPropertiesMap;
        if (CollectionUtils.isEmpty(properties.getForums())) {
            forumPropertiesMap =Collections.emptyMap();
        }
        else {
            forumPropertiesMap = new HashMap<>();
            for (ForumProperties forum : properties.getForums()) {
                if (CollectionUtils.isEmpty(forum.getDomains())) {
                    continue;
                }
                for (String domain : forum.getDomains()) {
                    forumPropertiesMap.put(domain, forum);
                }
            }
        }

        ConfigurationContext context = new ConfigurationContext();
        context.setIndexDirectory(properties.getIndexDirectory());
        context.setMapForumProperties(forumPropertiesMap);
        return context;
    }
}
