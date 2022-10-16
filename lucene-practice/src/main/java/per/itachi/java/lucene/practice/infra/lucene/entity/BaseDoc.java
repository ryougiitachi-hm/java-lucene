package per.itachi.java.lucene.practice.infra.lucene.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * not sure about whether it is a good design or not.
 * */
public abstract class BaseDoc {

    // The definition of field index type depends on whether the field can be queried from document or not.

    public static final int FLD_IDX_TYPE_TEXT = 1;

    public static final int FLD_IDX_TYPE_NUMBER = 2;

    public static final int FLD_IDX_TYPE_KEYWORD = 3;

    private Map<String, Integer> fieldIndexTypes = new HashMap<>();

    public void addFieldIndexType(String fieldName, int fieldIndexType) {
        this.fieldIndexTypes.put(fieldName, fieldIndexType);
    }

    public void removeFieldIndexType(String fieldName) {
        this.fieldIndexTypes.remove(fieldName);
    }
}
