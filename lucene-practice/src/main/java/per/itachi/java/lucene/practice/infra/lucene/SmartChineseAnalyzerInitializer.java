package per.itachi.java.lucene.practice.infra.lucene;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.HMMChineseTokenizer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.cn.smart.hhmm.HHMMSegmenter;
import org.springframework.stereotype.Component;
import per.itachi.java.lucene.practice.common.exception.CommonBusinessException;

@Slf4j
@Component
public class SmartChineseAnalyzerInitializer implements AnalyzerInitializer {

    private static final String FLD_TOEKN_FILTER_INPUT = "input";

    private static final String FLD_HMM_CHN_TOKENZIER_WORD_SEGMENTER = "wordSegmenter";

    private static final String FLD_WORD_SEGMENTER_HHMMSEGMENTER = "hhmmSegmenter";

    private static final String FLD_HHMMSEGMENTER_WORD_DICT = "wordDict";

    private static final String FLD_WORD_DICT_WORD_IDX_TBL = "wordIndexTable";

    private static final String FLD_WORD_DICT_CHAR_IDX_TBL = "charIndexTable";

    private static final String FLD_WORD_DICT_WORDITEM_CHAR_ARRAY_TBL = "wordItem_charArrayTable";

    private static final String FLD_WORD_DICT_WORDITEM_FREQUENCY_TBL = "wordItem_frequencyTable";

    private static final int FIXED_FREQUENCY = 128; // adjustable

    private String customWordsListPath = "input/analyzer-smartcn/cutsom-words.txt";

    @Override
    public Class<? extends Analyzer> supportsAnalyzer() {
        return SmartChineseAnalyzer.class;
    }

    @Override
    public void initialize(Analyzer analyzer) {
        if (! (analyzer instanceof SmartChineseAnalyzer) ) {
            return;
        }
        SmartChineseAnalyzer smartcn = (SmartChineseAnalyzer)analyzer;
//        Analyzer.TokenStreamComponents tokenStreamComponents = smartcn
//                .getReuseStrategy().getReusableComponents(smartcn, null); // null?
//        TokenStream tokenStream = tokenStreamComponents.getTokenStream();
        try(TokenStream tokenStream = analyzer.tokenStream("", "")) {
            HMMChineseTokenizer hmmcnTokenizer = extractHMMChineseTokenizer(tokenStream);
            Object wordSegmenter = extractVariableFromInstance(hmmcnTokenizer,
                    FLD_HMM_CHN_TOKENZIER_WORD_SEGMENTER, Object.class, true);
            HHMMSegmenter hhmmSegmenter = extractVariableFromInstance(wordSegmenter,
                    FLD_WORD_SEGMENTER_HHMMSEGMENTER, HHMMSegmenter.class, true);
            Object wordDict = extractVariableFromInstance(hhmmSegmenter,
                    FLD_HHMMSEGMENTER_WORD_DICT, Object.class, false); // WordDictionary, static member
            short[] wordIndexTable = extractVariableFromInstance(wordDict,
                    FLD_WORD_DICT_WORD_IDX_TBL, short[].class, false); // 12071
            char[] charIndexTable = extractVariableFromInstance(wordDict,
                    FLD_WORD_DICT_CHAR_IDX_TBL, char[].class, false); // 12071
//            char[][][] wordItemCharArrayTable = extractVariableFromInstance(wordDict,
//                    FLD_WORD_DICT_WORDITEM_CHAR_ARRAY_TBL, char[][][].class, false); // 8178
//            int[][] wordItemFrequencyTable = extractVariableFromInstance(wordDict,
//                    FLD_WORD_DICT_WORDITEM_FREQUENCY_TBL, int[][].class, false); // 8178

            SmartChineseWordDictionary smartChineseWordDictionary = new SmartChineseWordDictionary(wordDict);
            smartChineseWordDictionary.initialize();
//            smartChineseWordDictionary.showWordItem();
//            smartChineseWordDictionary.showWordItemCharArrayTable();

            NavigableSet<String> customWordsSet = loadCustomWordsSet();
            smartChineseWordDictionary.adjustWordDictionary(customWordsSet);
//            smartChineseWordDictionary.showWordItem();
        }
        catch (IOException e) {
            log.error("", e);
        }
    }

    private HMMChineseTokenizer extractHMMChineseTokenizer(TokenStream tokenStream) {
        TokenStream hmmcnTokenizer =  tokenStream;
        try {
            while (hmmcnTokenizer.getClass() != HMMChineseTokenizer.class) {
                Class<? extends TokenStream> clazz = hmmcnTokenizer.getClass();
//                Field[] fields = clazz.getDeclaredFields();
//                Field[] fields = clazz.getFields();
                Field fldTokenFilterInput = getDeclaredFieldFromClass(clazz, FLD_TOEKN_FILTER_INPUT);
                assert fldTokenFilterInput != null;
                hmmcnTokenizer = (TokenStream)fldTokenFilterInput.get(hmmcnTokenizer);
            }
        }
        catch (IllegalAccessException e) {
            log.error("Error ocurred. ", e);
        }
        if (hmmcnTokenizer.getClass() == HMMChineseTokenizer.class) {
            return (HMMChineseTokenizer) hmmcnTokenizer;
        }
        else {
            return null;
        }
    }

    private Field getDeclaredFieldFromClass(Class<?> clazz, String fieldName) {
        try {
            // It seems that below is not allowed since 17.
            Field modifiersField = generateModifiersFromField();
            do { // recursively get specific field from class.
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    field.setAccessible(true);
                    return field;
                }
                catch (NoSuchFieldException e) {
                    log.error("Failed to find the specific field [{}] from class {}. ", fieldName, clazz);
                    clazz = clazz.getSuperclass();
                }
            } while (clazz != Object.class);
        }
        catch (IllegalAccessException e) {
            log.error("Error occurred. ", e);
        }
        return null;
    }

    private <T> T extractVariableFromInstance(Object instance, String memberName, Class<T> memberClass, boolean isFinal) {
        try {
            Class<?> instanceClass = instance.getClass();
            Field fldMember = instanceClass.getDeclaredField(memberName);
            if (isFinal) {
                Field modifiersField = generateModifiersFromField();
                modifiersField.setInt(fldMember, fldMember.getModifiers() & ~Modifier.FINAL);
            }
            fldMember.setAccessible(true);
            T memberObj = memberClass.cast(fldMember.get(instance));
            return memberObj;
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error ocurred. ", e);
            return null;
        }
    }

    private void writeVariableViaInstance(Object instance, String memberName, boolean isFinal, Object newValue) {
        try {
            Class<?> instanceClass = instance.getClass();
            Field fldMember = instanceClass.getDeclaredField(memberName);
            if (isFinal) {
                Field modifiersField = generateModifiersFromField();
                modifiersField.setInt(fldMember, fldMember.getModifiers() & ~Modifier.FINAL);
            }
            fldMember.setAccessible(true);
            fldMember.set(instance, newValue);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error ocurred when trying to write new value into instance. ", e);
        }
    }

    private void writeWordDictBackToObject(short[] wordIndexTable, char[] charIndexTable,
                                           char[][][] wordItemCharArrayTable, int[][] wordItemFrequencyTable,
                                           Object wordDict) {
        List<Short> listWordIndexTable = new ArrayList<>(13000); // configurable
        List<Character> listCharIndexTable = new ArrayList<>(13000); // configurable
        List<char[][]> listWordItemCharArrayTable = new ArrayList<>(9000); // configurable
        List<int[]> listWordItemFrequencyTable = new ArrayList<>(9000); // configurable
        // a bit low
        for (short item: wordIndexTable) {
            listWordIndexTable.add(item);
        }
        for (char item: charIndexTable) {
            listCharIndexTable.add(item);
        }
        listWordItemCharArrayTable.addAll(Arrays.asList(wordItemCharArrayTable));
        listWordItemFrequencyTable.addAll(Arrays.asList(wordItemFrequencyTable));
    }

    private Field generateModifiersFromField() {
        try {
            // It seems that below is not allowed since 17.
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            return  modifiersField;
        }
        catch (NoSuchFieldException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }

    private NavigableSet<String> loadCustomWordsSet() {
        try {
            List<String> list = Files.readAllLines(Paths.get(customWordsListPath), StandardCharsets.UTF_8);
            NavigableSet<String> treeset = new TreeSet<>();
            for (String word : list) {
                if (word.trim().length() >= 1) {
                    treeset.add(word.trim());
                }
            }
            return treeset;
        }
        catch (IOException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }
}
