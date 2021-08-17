package per.itachi.java.lucene.sample;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

public class SampleApplication {

    public static void main(String[] args) {

    }

    /**
     * @return list all of thread urls.
     * */
    private List<String> listAllThreadUrls() {
        return Collections.emptyList();
    }

    /**
     * @param inputDocumentPaths
     * @param outputIndicesPath
     * @since
     * */
    private void writeIndices(List<String> inputDocumentPaths, String outputIndicesPath) {
        if (CollectionUtils.isEmpty(inputDocumentPaths)) {
            // logger.error
            return;
        }
        if (!StringUtils.hasText(outputIndicesPath)) {
            // logger.error
            return;
        }
        for (String inputDocumentPath : inputDocumentPaths) {
        }
    }
}
