package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import per.itachi.java.lucene.sample.TestConstants;
import per.itachi.java.lucene.sample.entity.PostBreakpointRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
public class FileBreakpointRecorderTest {

    @Autowired
    private BreakpointRecorder fileBreakpointRecorder;

    @Test
    public void writeAll() {
        String envForumName = System.getenv(TestConstants.ENV_FORUM_NAME);
        List<PostBreakpointRecord> list = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            list.add(PostBreakpointRecord.builder()
                    .postId(System.currentTimeMillis())
                    .build());
        }
        fileBreakpointRecorder.writeAll(envForumName, list);
    }

    @Test
    public void readAll() {
        String envForumName = System.getenv(TestConstants.ENV_FORUM_NAME);
        Set<PostBreakpointRecord> result = fileBreakpointRecorder.readAll(envForumName);
        log.info("readAll is {} {}. ", result.size(), result);
    }

    @Configuration
    @ComponentScan({"per.itachi.java.lucene.sample.configuration",
            "per.itachi.java.lucene.sample.component"})
    static class TestConfiguration {

        @Bean
        public BreakpointRecorder fileBreakpointRecorder() {
            return new FileBreakpointRecorder();
        }
    }
}
