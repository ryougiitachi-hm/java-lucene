package per.itachi.java.lucene.practice.infra.restful.config;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestfulDownloaderProperties {

    private int defaultBufferSize;

    private Map<String, String> sites;

    @Getter
    @Setter
    public static class DownloaderSiteProperties {

        private String websiteName;

        private List<String> domains;

        private Map<String, String> headers;
    }
}
