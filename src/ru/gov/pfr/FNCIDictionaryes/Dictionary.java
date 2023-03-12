package ru.gov.pfr.FNCIDictionaryes;

import java.util.HashMap;

/**
 *
 *
 * @author kneretin
 */
public abstract class Dictionary {

    public HashMap<String, String> map = new HashMap<>();
    private static Dictionary instance;

    protected static void addValue(String key, String value) {
        instance.map.put(key, value);
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public void print() {

    }

}
