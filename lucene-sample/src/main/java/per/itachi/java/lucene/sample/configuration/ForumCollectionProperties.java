package per.itachi.java.lucene.sample.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ForumCollectionProperties {

    @JsonProperty("index-directory")
    private String indexDirectory;

    private List<ForumProperties> forums;
}
