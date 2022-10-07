package per.itachi.java.lucene.practice.infra.lucene;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import per.itachi.java.lucene.practice.common.exception.CommonBusinessException;

@Slf4j
public class SmartChineseWordDictionary {

    private static final String METHOD_HASH_FNV32_CHAR = "hash1";

    private static final String METHOD_HASH_DJB2_CHAR = "hash2";

    private static final String METHOD_GET_AVALIABLE_TABLE_INDEX = "getAvaliableTableIndex";

    private static final String METHOD_GET_UTF_BY_GB2312 = "getCCByGB2312Id";

    private static final String METHOD_GET_GB2312_BY_UTF = "getGB2312Id";

    private static final String FLD_WORD_IDX_TBL = "wordIndexTable";

    private static final String FLD_CHAR_IDX_TBL = "charIndexTable";

    private static final String FLD_WORDITEM_CHAR_ARRAY_TBL = "wordItem_charArrayTable";

    private static final String FLD_WORDITEM_FREQUENCY_TBL = "wordItem_frequencyTable";

    private static final int FIXED_FREQUENCY = 128;

    private Object wordDictionary;

    private Map<String, Field> fieldMap;

    private Map<String, Method> methodMap;

    public SmartChineseWordDictionary(Object wordDictionary) {
        this.wordDictionary = wordDictionary;
    }

    @PostConstruct
    public void initialize() {
        initializeFields();
        initializeMethods();
    }

    private void initializeFields() {
        Class<?> clazz = this.wordDictionary.getClass();
        Map<String, Field> fieldMap = new HashMap<>();
        fieldMap.put(FLD_WORD_IDX_TBL, extractFieldFromInstance(wordDictionary, FLD_WORD_IDX_TBL, false));
        fieldMap.put(FLD_CHAR_IDX_TBL, extractFieldFromInstance(wordDictionary, FLD_CHAR_IDX_TBL, false));
        fieldMap.put(FLD_WORDITEM_CHAR_ARRAY_TBL, extractFieldFromInstance(wordDictionary, FLD_WORDITEM_CHAR_ARRAY_TBL, false));
        fieldMap.put(FLD_WORDITEM_FREQUENCY_TBL, extractFieldFromInstance(wordDictionary, FLD_WORDITEM_FREQUENCY_TBL, false));
        this.fieldMap = fieldMap;
    }

    private void initializeMethods() {
        Class<?> clazz = this.wordDictionary.getClass();
        Map<String, Method> methodMap = new HashMap<>();
        try {
            Method method = null;
            // getAvaliableTableIndex
            method = clazz.getDeclaredMethod(METHOD_GET_AVALIABLE_TABLE_INDEX, char.class);
            method.setAccessible(true);
            methodMap.put(METHOD_GET_AVALIABLE_TABLE_INDEX, method);
            // getCCByGB2312Id
            method = clazz.getMethod(METHOD_GET_UTF_BY_GB2312, int.class);
            method.setAccessible(true);
            methodMap.put(METHOD_GET_UTF_BY_GB2312, method);
            // getGB2312Id
            method = clazz.getMethod(METHOD_GET_GB2312_BY_UTF, char.class);
            method.setAccessible(true);
            methodMap.put(METHOD_GET_GB2312_BY_UTF, method);
            this.methodMap = methodMap;
        }
        catch (NoSuchMethodException e) {
            log.error("Failed to initialize. ",e );
            throw new CommonBusinessException(e);
        }
    }

    private Field extractFieldFromInstance(Object instance, String memberName, boolean isFinal) {
        try {
            Class<?> instanceClass = instance.getClass();
            Field fldMember = instanceClass.getDeclaredField(memberName);
            if (isFinal) {
                Field modifiersField = generateModifiersFromField();
                modifiersField.setInt(fldMember, fldMember.getModifiers() & ~Modifier.FINAL);
            }
            fldMember.setAccessible(true);
            return fldMember;
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error ocurred. ", e);
            throw new CommonBusinessException(e);
        }
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

    public void showWordItem() {
        int i = 0;
        try {
            char[][][] wordItemCharArrayTable = (char[][][])fieldMap.get(FLD_WORDITEM_CHAR_ARRAY_TBL).get(wordDictionary);
            int[][] wordItemFrequencyTable = (int[][])fieldMap.get(FLD_WORDITEM_FREQUENCY_TBL).get(wordDictionary);
            int delimiterIndex = 3755 + 1410; // 1410 GB2312_FIRST_CHAR
            char[][] delimiterArray = wordItemCharArrayTable[delimiterIndex];
            log.info("The wordItemCharArrayTable are as follow: ");
            for (char[][] charArrayTable : wordItemCharArrayTable) {
                if (charArrayTable != null && charArrayTable.length > 0) {
                    StringBuilder builder = new StringBuilder();
                    int j = 0;
                    for (char[] itemWord: charArrayTable) {
                        if (itemWord == null) {
                            builder.append("null");
                        }
                        else {
                            builder.append(new String(itemWord));
                        }
                        builder.append("/").append(wordItemFrequencyTable[i][j]).append(", ");
                        ++j;
                    }
                    builder.append("\b\b");
                    log.info("{} {}: {}", String.format("%05d",i), getCCByGB2312Id(i), builder);
                }
                ++i;
            }
        }
        catch (Exception e) {
            log.error("Error occurred, i={}. ", i, e);
            throw new CommonBusinessException(e);
        }
    }

    public void showWordItemCharArrayTable() {
        try {
            char[][][] wordItemCharArrayTable = (char[][][])fieldMap.get(FLD_WORDITEM_CHAR_ARRAY_TBL).get(wordDictionary);
            int delimiterIndex = 3755 + 1410; // 1410 GB2312_FIRST_CHAR
            char[][] delimiterArray = wordItemCharArrayTable[delimiterIndex];
            int i = 0;
            log.info("The delimiterArray are as follow: ");
            if (delimiterArray != null) {
                for (char[] item : delimiterArray) {
                    String strItem = new String(item);
                    log.info("delimiterArray[{}]={}", ++i, strItem);
                }
            }
            String cc = getCCByGB2312Id(1536);
            int gb2312id = getGB2312Id('夯');
            log.info("cc={}, gb2312id={}", cc, gb2312id);
        }
        catch (IllegalAccessException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }

    public String getCCByGB2312Id(int ccid) {
        Method method = methodMap.get(METHOD_GET_UTF_BY_GB2312);
        try {
            return (String)method.invoke(wordDictionary, ccid);
        }
        catch (IllegalAccessException|InvocationTargetException e) {
            throw new CommonBusinessException(e);
        }
    }

    public short getGB2312Id(char ch) {
        Method method = methodMap.get(METHOD_GET_GB2312_BY_UTF);
        try {
            return (short)method.invoke(wordDictionary, ch);
        }
        catch (IllegalAccessException|InvocationTargetException e) {
            throw new CommonBusinessException(e);
        }
    }

    public void adjustWordDictionary(NavigableSet<String> customWordsSet) {
        Map<Integer, List<char[]>> mapGB2312ToWords = new HashMap<>();

        // initialize the list of custom words.
        for (String word: customWordsSet) {
            // ascending by default
            log.info("word={}", word);
            int gb2312id = this.getGB2312Id(word.charAt(0));
            if (!mapGB2312ToWords.containsKey(gb2312id)) {
                mapGB2312ToWords.put(gb2312id, new ArrayList<>());
            }
            if (word.substring(1).length() <= 0) {
                // TODO: necessary?
                continue;
            }
            mapGB2312ToWords.get(gb2312id).add(word.substring(1).toCharArray());
        }

        // adjust
        try {
            char[][][] wordItemCharArrayTable = wordItemCharArrayTable = (char[][][])fieldMap
                    .get(FLD_WORDITEM_CHAR_ARRAY_TBL).get(this.wordDictionary);
            int[][] wordItemFrequencyTable = (int[][])fieldMap
                    .get(FLD_WORDITEM_FREQUENCY_TBL).get(this.wordDictionary);
            for (Map.Entry<Integer, List<char[]>> entry : mapGB2312ToWords.entrySet()) {
                // FLD_WORD_DICT_WORDITEM_CHAR_ARRAY_TBL
                char[][] words = wordItemCharArrayTable[entry.getKey()];
                List<char[]> wordList = new ArrayList<>(Arrays.asList(words));
                wordList.addAll(entry.getValue());
//                wordItemCharArrayTable[entry.getKey()] = wordList.toArray(new char[0][]);
                // FLD_WORD_DICT_WORDITEM_FREQUENCY_TBL
                int[] wordsFrequecy = wordItemFrequencyTable[entry.getKey()];
                int[] wordsFrequecyNew = Arrays.copyOf(wordsFrequecy, wordsFrequecy.length + entry.getValue().size());
                Arrays.fill(wordsFrequecyNew, wordsFrequecy.length, wordsFrequecyNew.length, FIXED_FREQUENCY);
//                wordItemFrequencyTable[entry.getKey()] = wordsFrequecyNew;
                // sort
                int idx = 0;
                List<WordToken> wordTokenList = new ArrayList<>();
                for(char[] word : wordList) {
                    wordTokenList.add(new WordToken(word== null? null : new String(word), wordsFrequecyNew[idx]));
                    ++idx;
                }
                Collections.sort(wordTokenList);
                // write back
                char[][] wordsNew = new char[wordTokenList.size()][];
                for (idx = 0; idx < wordTokenList.size(); ++idx) {
                    wordsNew[idx] = wordTokenList.get(idx).getWord() == null
                            ? null : wordTokenList.get(idx).getWord().toCharArray();
                    wordsFrequecyNew[idx] = wordTokenList.get(idx).getFrequency();
                }
                wordItemCharArrayTable[entry.getKey()] = wordsNew;
                wordItemFrequencyTable[entry.getKey()] = wordsFrequecyNew;
            }
        }
        catch (IllegalAccessException e) {
            log.error("Error occurred. ", e);
            throw new CommonBusinessException(e);
        }
    }

    @Getter
    @Setter
    private static class WordToken implements Comparable<WordToken> {

        private String word;

        private int frequency;

        public WordToken(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(WordToken o) {
            if (this.word == null) {
                return 1;
            }
            if (o.word == null) {
                return -1;
            }
            return this.word.compareTo(o.word);
        }
    }
}
