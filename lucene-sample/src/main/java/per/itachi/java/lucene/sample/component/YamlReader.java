package per.itachi.java.lucene.sample.component;

import per.itachi.java.lucene.sample.configuration.ForumCollectionProperties;

public interface YamlReader {

    ForumCollectionProperties readFromResource(String resource);
}
