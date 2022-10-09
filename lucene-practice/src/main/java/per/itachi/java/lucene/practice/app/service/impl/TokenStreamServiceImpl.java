package per.itachi.java.lucene.practice.app.service.impl;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.springframework.stereotype.Service;
import per.itachi.java.lucene.practice.app.service.TokenStreamService;
import per.itachi.java.lucene.practice.infra.file.text.LineReader;
import per.itachi.java.lucene.practice.infra.lucene.AnalyzerInitializer;

@Slf4j
@Service
public class TokenStreamServiceImpl implements TokenStreamService {

    @Resource
    private LineReader lineReader;

    @Resource
    private AnalyzerInitializer analyzerInitializer;

    @Override
    public void outputTokenStreamFromTxtFile(String fileName) {
        List<String> lines = lineReader.listAllLines(fileName);

        Analyzer analyzer = new SmartChineseAnalyzer();
//        Analyzer analyzer = new MMSegAnalyzer();
        analyzerInitializer.initialize(analyzer);

        for (String line : lines) {
            try(TokenStream tokenStream = analyzer.tokenStream("content", line)) {
                CharTermAttribute cta = tokenStream.addAttribute(CharTermAttribute.class);
                //位置增量的属性，存储词之间的距离
                PositionIncrementAttribute pia = tokenStream.addAttribute(PositionIncrementAttribute.class);
                //储存每个词直接的偏移量
                OffsetAttribute oa = tokenStream.addAttribute(OffsetAttribute.class);
                //使用的每个分词器直接的类型信息
                TypeAttribute ta = tokenStream.addAttribute(TypeAttribute.class);
                try {
                    tokenStream.reset();
                    while (tokenStream.incrementToken()) {
                        log.info("cta={}, [+oa.startOffset={}, oa.endOffset={}], ta.type={}, pia.positionIncrement={}. ",
                                cta, oa.startOffset(), oa.endOffset(), ta.type(), pia.getPositionIncrement());
                    }
                    tokenStream.end();
                }
                catch (IOException e) {
                    log.error("Error occurred. ", e);
                }
            }
            catch (IOException e) {
                log.error("Error occurred. ", e);
            }
        }
    }
}