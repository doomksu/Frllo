package ru.gov.pfr.utils;

import java.math.BigDecimal;

/**
 *
 * @author Admin
 */
public class XMLValues {

    private String value;

    public XMLValues() {
        value = "";
    }

    public XMLValues(String str) {
        if (str.contains(">") && str.contains("</")) {
            if (str.indexOf("</") > (str.indexOf(">") + 1)) {
                value = str.substring(str.indexOf(">") + 1, str.indexOf("</"));
            } else {
                value = "";
            }
        } else {
            value = "";
        }
    }

    /**
     * Получить значение XML тега
     *
     * @param String str
     * @return String val
     */
    public static String getValue(String str) {
        String val = "";
        if (str.contains(">") && str.contains("</")) {
            if (str.indexOf("</") > (str.indexOf(">") + 1)) {
                val = str.substring(str.indexOf(">") + 1, str.indexOf("</"));
            }
        }
        return val;
    }

    static String getValue(StringBuilder str) {
        return str.substring(str.indexOf(">") + 1, str.indexOf("</"));
    }

    public static String getTagName(String str) {
        if (str != null && !str.isEmpty()) {
            return str.substring(str.indexOf("<") + 1, str.indexOf(">"));
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public BigDecimal getBigDecimalValue() {
        return new BigDecimal(value);
    }

    /**
     * Заменить значение в теге новым значением
     *
     * @param teg
     * @param newValue
     * @return
     */
    public static String replaceValue(String teg, String newValue) {
        String replaced = "";
        replaced = teg.replace(XMLValues.getValue(teg), newValue);
        return replaced;
    }

    public static boolean isSingleTag(String str) {
        String string = str.trim();
        if (string.contains("<") == false && string.contains(">") == false) {
            return false;
        }
        int first_tag_finish = string.indexOf(">") + 1;
        if (first_tag_finish == string.length()) {
            return true;
        }
        int first_tag_start = string.indexOf("<");
        String tagText = string.substring(first_tag_start + 1, first_tag_finish - 1);
        if (string.contains("/" + tagText) == false) {
            return true;
        }
        return false;
    }
}
