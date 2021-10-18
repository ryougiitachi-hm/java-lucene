package per.itachi.java.lucene.sample.parser;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.html.PostInfo;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JsoupForumParserTest.TestConfiguration.class)
public class JsoupForumParserTest {

    private static final String ENV_FORUM_NAME = "FORUM_NAME";

    @Autowired
    private ConfigurationContext context;
    
	@Autowired
	private ForumParser parser;
	
	@Test
	public void parsePost() {
        String strEnvName = System.getenv(ENV_FORUM_NAME);
        ForumProperties properties = context.getForumPropertiesByName(strEnvName);
		Path htmlPath = Paths.get("pages/siszzo/siszzo-post.html");
		PostInfo postInfo = parser.parsePost(htmlPath, properties);
		log.info("PostInfo is {}. ", postInfo);
	}
	
    @Configuration
    @ComponentScan({"per.itachi.java.lucene.sample.configuration", 
    	"per.itachi.java.lucene.sample.component"})
    static class TestConfiguration {
		
		@Bean
		public ForumParser parser() {
			return new JsoupForumParser();
		}
	}
}
