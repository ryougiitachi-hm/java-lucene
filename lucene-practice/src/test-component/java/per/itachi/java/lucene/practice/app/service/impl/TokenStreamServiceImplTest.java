package per.itachi.java.lucene.practice.app.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TokenStreamServiceImplTest.TestConfiguration.class)
public class TokenStreamServiceImplTest {

    @Autowired
    private TokenStreamServiceImpl tokenStreamService;

    @Test
    public void outputTokenStreamFromTxtFile() {
        String filePath = System.getenv("FILE_PATH");
        tokenStreamService.outputTokenStreamFromTxtFile(filePath);
    }

    @Configuration
    @ComponentScan({"per.itachi.java.lucene.practice"})
    static class TestConfiguration {}
}
