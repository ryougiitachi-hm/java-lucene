package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import per.itachi.java.lucene.sample.TestConstants;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.configuration.ForumProperties;
import per.itachi.java.lucene.sample.entity.PostBreakpointRecord;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
public class FileBreakpointRecorderTest {

    @Autowired
    private ConfigurationContext context;

    @Autowired
    private BreakpointRecorder fileBreakpointRecorder;

//    @Test
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

//    @Test
    public void readAll() {
        String envForumName = System.getenv(TestConstants.ENV_FORUM_NAME);
        Set<PostBreakpointRecord> result = fileBreakpointRecorder.readAll(envForumName);
        log.info("readAll is {} {}. ", result.size(), result);
    }

    @Test
    public void fixPostBreakpointFile() {
        String envForumName = System.getenv(TestConstants.ENV_FORUM_NAME);
        ForumProperties properties = context.getForumPropertiesByName(envForumName);
        Path postsPath = Paths.get(context.getHtmlDirectory(), envForumName, context.getPostDirectory());
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(postsPath)) {
            List<PostBreakpointRecord> list = new LinkedList<>();
            log.info("Traversing post list. ");
            for (Path filePath : stream) {
                list.add(PostBreakpointRecord.builder()
                        .postId(generatePageId(filePath.getFileName().toString(),
                                properties.getPostParams()))
                        .build());
            }
            log.info("Traversed {} post. ", list.size());
            log.info("Writing ... ");
            fileBreakpointRecorder.writeAll(envForumName, list);
            log.info("Wrote. ");
        }
        catch (IOException e) {
            log.error("", e);
        }
    }

    private long generatePageId(String fileName, List<String> params) {
        if (CollectionUtils.isEmpty(params)) {
            return 0;
        }
        int idxTag = fileName.indexOf(params.get(0));
        int idxHythen = fileName.indexOf("-", idxTag);
        int idxSuffix = fileName.indexOf(".", idxTag);
        String strPostId;
        if (idxHythen >= 0) {
            strPostId = fileName.substring(idxTag + params.get(0).length(), idxHythen);
        }
        else if (idxSuffix >= 0) {
            strPostId = fileName.substring(idxTag + params.get(0).length(), idxSuffix);
        }
        else {
            strPostId = "";
        }
        return Long.parseLong(strPostId);
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
