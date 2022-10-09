package per.itachi.java.lucene.practice.infra.file.text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LineReader {

    public List<String> listAllLines(String fileName) {
        try(
//                InputStreamReader isr = new FileReader(fileName, StandardCharsets.UTF_8); // since 11
                InputStreamReader isr = new InputStreamReader(Files.newInputStream(Paths.get(fileName)), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(isr);) {
            List<String> results = new ArrayList<>();
            String strLine = null;
            while ( (strLine = bufferedReader.readLine()) != null) {
                results.add(strLine);
            }
            return results;
        }
        catch (IOException ioe) {
            log.error("Error occurred when reading the text file, fileName={}", fileName, ioe);
            return Collections.emptyList();
        }
    }
}