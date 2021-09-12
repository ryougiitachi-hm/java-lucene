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

    @JsonProperty("html-directory")
    private String htmlDirectory;

    @JsonProperty("index-directory")
    private String indexDirectory;

    @JsonProperty("buffer-reader-size")
    private int bufferReaderSize;

    @JsonProperty("buffer-writer-size")
    private int bufferWriterSize;

    private List<ForumProperties> forums;
}
