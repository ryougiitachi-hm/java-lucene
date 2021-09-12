package per.itachi.java.lucene.sample.service.imple;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import per.itachi.java.lucene.sample.service.IndicesService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = IndicesServiceImplTest.TestConfiguration.class)
public class IndicesServiceImplTest {

    private static final String ENV_FORUM_CATEGORY_URL = "FORUM_CATEGORY_URL";

    @Autowired
    private IndicesService indicesService;


    @Test
    public void updateIndices() {
        String strEnvCategoryUrl = System.getenv(ENV_FORUM_CATEGORY_URL);
        indicesService.updateIndices(strEnvCategoryUrl);
    }

    @Configuration
    @ComponentScan("per.itachi.java.lucene.sample")
    static class TestConfiguration {}
}
