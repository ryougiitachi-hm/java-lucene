package per.itachi.java.lucene.practice.infra.lucene.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import per.itachi.java.lucene.practice.infra.lucene.AnalyzerInitializer;

@Configuration
public class LuceneConfig {

    @Bean
    @ConfigurationProperties("infra.lucene")
    public LuceneProperties luceneProperties() {
        return new LuceneProperties();
    }

    /**
     * not sure about whether or not Analyzer is thread-safe.
     * */
    @Bean(destroyMethod = "close")
    public Analyzer smartChineseAnalyzer(AnalyzerInitializer analyzerInitializer) {
        Analyzer analyzer = new SmartChineseAnalyzer();
        analyzerInitializer.initialize(analyzer);
        return analyzer;
    }
}
