package per.itachi.java.lucene.sample.parser;

import per.itachi.java.lucene.sample.entity.PostBreakpointRecord;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public interface BreakpointRecorder {

    void write(String name, PostBreakpointRecord postRecord);

    void writeAll(String name, Collection<PostBreakpointRecord> records);

    /**
     * @return result
     * */
    PostBreakpointRecord read(String name);

    Set<PostBreakpointRecord> readAll(String name);
}
