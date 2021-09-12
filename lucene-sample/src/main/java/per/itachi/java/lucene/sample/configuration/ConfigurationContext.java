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

    private String indexDirectory;

    private int bufferReaderSize;

    private int bufferWriterSize;

    private Map<String, ForumProperties> mapForumProperties;

    public ForumProperties getForumPropertiesByDomain(String domain) {
        if (CollectionUtils.isEmpty(mapForumProperties)) {
            return null;
        }
        return mapForumProperties.get(domain);
    }
}
