package per.itachi.java.lucene.sample.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import per.itachi.java.lucene.sample.configuration.ConfigurationContext;
import per.itachi.java.lucene.sample.entity.PostBreakpointRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class FileBreakpointRecorder implements BreakpointRecorder{

    private static final String BREAKPOINT_FILE_NAME = "downloaded-records-storage.dat";

    @Autowired
    private ConfigurationContext context;

    @Override
    public void write(String name, PostBreakpointRecord postRecord) {
//        context.getForumPropertiesByName(name);
        if (!checkDataDirectory(context.getDataDirectory(), name)) {
            return;
        }

        Path breakpointFilePath = Paths.get(context.getDataDirectory(), name, BREAKPOINT_FILE_NAME);
        try(DataOutputStream oos = new DataOutputStream(Files.newOutputStream(breakpointFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            oos.writeLong(postRecord.getPostId());
        }
        catch (IOException e) {
            log.error("[BreakpointRecorder] Error occurred when writing {} into data file {}. ",
                    postRecord, breakpointFilePath, e);
        }
    }

    @Override
    public void writeAll(String name, Collection<PostBreakpointRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            log.info("[BreakpointRecorder] There is no record to update. ");
            return;
        }

        if (!checkDataDirectory(context.getDataDirectory(), name)) {
            return;
        }

        Path breakpointFilePath = Paths.get(context.getDataDirectory(), name, BREAKPOINT_FILE_NAME);
        try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(breakpointFilePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            for (PostBreakpointRecord record : records) {
                dos.writeLong(record.getPostId());
            }
            log.info("[BreakpointRecorder] Wrote {} records. ", records.size());
        }
        catch (IOException e) {
            log.error("[BreakpointRecorder] Error occurred when writing multiple records into data file {}. ",
                    breakpointFilePath, e);
        }
    }

    private boolean checkDataDirectory(String dataDir, String name) {
        Path dirPath = Paths.get(dataDir, name);
        // check whether data dir exists or not.
        if (!Files.exists(dirPath)) {
            try {
                // Files.createDirectory for creating single level.
                Files.createDirectories(dirPath);
                log.info("The data path {} didn't exist, and is created. ", dirPath);
                return true;
            }
            catch (IOException e) {
                log.error("Failed to create data path {}. ", dirPath, e);
                return false;
            }
        }

        if (!Files.isDirectory(dirPath)) {
            log.error("The path {} is not a directory. ", dirPath);
            return false;
        }

        return true;// dead code?
    }

    @Override
    public PostBreakpointRecord read(String name) {
        Path breakpointFilePath = Paths.get(context.getDataDirectory(), name, BREAKPOINT_FILE_NAME);
        if (!Files.exists(breakpointFilePath)) {
            log.info("There hasn't been any breakpoint record yet. ");
            return null;
        }
        try(DataInputStream dis = new DataInputStream(Files.newInputStream(breakpointFilePath))) {
            return PostBreakpointRecord.builder()
                    .postId(dis.readLong()) // post id
                    .build();
        }
        catch (IOException e) {
            log.error("[BreakpointRecorder] Error occurred when reading record from data file {}. ",
                    breakpointFilePath, e);
            return null;
        }
    }

    @Override
    public Set<PostBreakpointRecord> readAll(String name) {
        Path breakpointFilePath = Paths.get(context.getDataDirectory(), name, BREAKPOINT_FILE_NAME);
        if (!Files.exists(breakpointFilePath)) {
            log.info("There hasn't been any breakpoint record yet. ");
            return null;
        }
        Set<PostBreakpointRecord> result = Collections.emptySet();
        try(DataInputStream dis = new DataInputStream(Files.newInputStream(breakpointFilePath))) {
            result = new HashSet<>();
            boolean running = true;
            while (running) {// until throws EOF.
                result.add(PostBreakpointRecord.builder()
                        .postId(dis.readLong())
                        .build());
            }
            return result;
        }
        catch (EOFException e) {
            log.error("Shows the information about EOF: {}. ", e.getMessage());
            // returns result when end of file reaches.
            return result;
        }
        catch (IOException e) {
            log.error("[BreakpointRecorder] Error occurred when reading record from data file {}. ",
                    breakpointFilePath, e);
            return Collections.emptySet();
        }
    }
}
