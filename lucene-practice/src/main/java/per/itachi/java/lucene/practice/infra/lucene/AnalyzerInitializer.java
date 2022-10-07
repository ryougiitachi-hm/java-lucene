package per.itachi.java.lucene.practice.infra.lucene;

import org.apache.lucene.analysis.Analyzer;

public interface AnalyzerInitializer {

    Class<? extends Analyzer> supportsAnalyzer();

    void initialize(Analyzer analyzer);
}
