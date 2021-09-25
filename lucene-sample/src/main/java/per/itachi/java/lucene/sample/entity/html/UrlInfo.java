package per.itachi.java.lucene.sample.entity.html;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class UrlInfo {

    private String host;

    private String baseUri;

    private String baseRelativeUri;

    private List<String> paths;

    private String fileName;

    private Map<String, String> params;
}