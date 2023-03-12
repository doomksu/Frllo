package ru.gov.pfr.xmlStructure;

import java.util.HashMap;

public class MapXMLContainer {

    protected String wrapperTeg = "";
    protected HashMap<String, String> map;
    protected String[] keysQueue = new String[]{};

    public String getTeg(String tegName) {
        if (map.containsKey(tegName)) {
            if (map.get(tegName) == null || map.get(tegName).isEmpty()) {
                return "<" + tegName + "/>";
            } else {
                return "<" + tegName + ">" + map.get(tegName) + "</" + tegName + ">";
            }
        }
        return "";
    }

    protected StringBuilder fillQueueTegs() {
        StringBuilder val = new StringBuilder();
        for (String key : keysQueue) {
            if (key != null && !key.isEmpty()) {
                val.append(getTeg(key) + "\r\n");
            }
        }
        return val;
    }

    @Override
    public String toString() {
        StringBuilder val = new StringBuilder();
        if (wrapperTeg != null && wrapperTeg.isEmpty() == false) {
            val.append("<" + wrapperTeg + ">\r\n");
        }
        val.append(fillQueueTegs());
        if (wrapperTeg != null && wrapperTeg.isEmpty() == false) {
            val.append("</" + wrapperTeg + ">\r\n");
        }
        return val.toString();
    }
}
