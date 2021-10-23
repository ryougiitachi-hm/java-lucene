package per.itachi.java.lucene.sample.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Getter
@Setter
@ToString
public class ConfigurationContext {

    private String htmlDirectory;

    private String categoryDirectory;

    private String postDirectory;

    private String indexDirectory;

    private String dataDirectory;

    private int bufferReaderSize;

    private int bufferWriterSize;

    private Map<String, ForumProperties> mapForumProperties;

    private Map<String, ForumProperties> mapForumPropertiesByName;

    public ForumProperties getForumPropertiesByDomain(String domain) {
        if (CollectionUtils.isEmpty(mapForumProperties)) {
            return null;
        }
        return mapForumProperties.get(domain);
    }

    public ForumProperties getForumPropertiesByName(String name) {
        if (CollectionUtils.isEmpty(mapForumPropertiesByName)) {
            return null;
        }
        return mapForumPropertiesByName.get(name);

    }
}
