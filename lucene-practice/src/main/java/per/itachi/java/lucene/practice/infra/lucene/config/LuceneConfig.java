package per.itachi.java.lucene.practice.infra.lucene.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {

    @Bean
    @ConfigurationProperties("infra.lucene")
    public LuceneProperties luceneProperties() {
        return new LuceneProperties();
    }
}
