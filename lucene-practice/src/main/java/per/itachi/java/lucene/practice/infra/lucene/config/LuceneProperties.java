package per.itachi.java.lucene.practice.infra.lucene.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LuceneProperties {

    private String basePath;

    private int commitDocThreshold;

//    private Map<String, AnalyzerInitializerProperties> analyzerInitializer;

    public static class AnalyzerInitializerProperties {
    }
}
