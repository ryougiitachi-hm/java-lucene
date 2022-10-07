package per.itachi.java.lucene.practice.infra.lucene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class AnalyzerInitializerProxy implements AnalyzerInitializer {

    @Autowired
    private List<AnalyzerInitializer> analyzerInitializerList;

    private Map<Class<? extends Analyzer>, AnalyzerInitializer> analyzerInitializerMap;

    @PostConstruct
    public void init() {
        Map<Class<? extends Analyzer>, AnalyzerInitializer> analyzerInitializerMap = new HashMap<>();
        for (AnalyzerInitializer analyzerInitializer : this.analyzerInitializerList) {
            if (analyzerInitializer.supportsAnalyzer() == null) {
                continue;
            }
            analyzerInitializerMap.put(analyzerInitializer.supportsAnalyzer(), analyzerInitializer);
        }
        this.analyzerInitializerMap = analyzerInitializerMap;
    }

    @Override
    public Class<? extends Analyzer> supportsAnalyzer() {
        return null;
    }

    @Override
    public void initialize(Analyzer analyzer) {
        AnalyzerInitializer analyzerInitializer = this.analyzerInitializerMap.get(analyzer.getClass());
        if (analyzerInitializer == null) {
            return;
        }
        analyzerInitializer.initialize(analyzer);
    }
}
