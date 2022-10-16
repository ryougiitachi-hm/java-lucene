package per.itachi.java.lucene.practice.infra.restful.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestfulConfig {

    @Bean
    @ConfigurationProperties("infra.restful.downloader")
    public RestfulDownloaderProperties restfulDownloaderProperties() {
        return new RestfulDownloaderProperties();
    }
}
