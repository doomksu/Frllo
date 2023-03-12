package ru.gov.pfr.xmlStructure.blocs;

import java.util.HashMap;
import ru.gov.pfr.xmlStructure.MapXMLContainer;

/**
 *
 * @author kneretin
 */
public class Address extends MapXMLContainer {

    private static String[] checkedFields = {"prefix_area", "area", "sreet", "prefix_sreet", "house_num"};

    public Address(HashMap<String, String> personMap, int typeOfAddres, String region) {
        init();
        map.put("address_type_code", String.valueOf(typeOfAddres));
        map.put("region", region);

        if (typeOfAddres == 3) {
            map.put("prefix_area", personMap.get("FAKT_GOROD"));
            map.put("area", personMap.get("FAKT_PUNKT"));
            map.put("sreet", personMap.get("FAKT_UL"));
            map.put("prefix_sreet", "");
            map.put("house_num", personMap.get("FAKT_DOM"));
            map.put("build_num", "");
            map.put("struc_num", personMap.get("FAKT_KOR"));
            map.put("room_num", personMap.get("FAKT_KVA"));
        }
        if (typeOfAddres == 1) {
            map.put("prefix_area", personMap.get("REG_GOROD"));
            map.put("area", personMap.get("REG_PUNKT"));
            map.put("sreet", personMap.get("REG_UL"));
            map.put("prefix_sreet", "");
            map.put("house_num", personMap.get("REG_DOM"));
            map.put("build_num", "");
            map.put("struc_num", personMap.get("REG_KOR"));
            map.put("room_num", personMap.get("REG_KVA"));
        }
        tryPrefixAreaFromPunkt();
        tryPrefixStreetFromStreet();
        map.put("live_start_date", personMap.get("SROKS"));
    }

    private void init() {
        wrapperTeg = "address";
        keysQueue = new String[]{
            "address_type_code",
            "region",
            "prefix_area",
            "area",
            "prefix_sreet",
            "sreet",
            "house_num",
            "build_num",
            "struc_num",
            "room_num",
            "live_start_date"};
        map = new HashMap<>();
    }

    public boolean isValid() {
        for (String key : checkedFields) {
            if (map.get(key) == null || map.get(key).isEmpty()) {
//                LoggingService.writeLog("bad address: " + toString(), "debug");
                return false;
            }
        }
//        LoggingService.writeLog("good address: " + toString(), "debug");
        return true;
    }

    @Override
    public String toString() {
        StringBuilder val = new StringBuilder();
        val.append("<addresses>\r\n");
        val.append(super.toString());
        val.append("</addresses>\r\n");
        return val.toString();
    }

    private void tryPrefixAreaFromPunkt() {
        if (map.get("prefix_area").isEmpty()) {
            if (map.get("area") != null && map.get("area").isEmpty() == false) {
                if (map.get("area").toLowerCase().startsWith("г.")
                        || map.get("area").toLowerCase().startsWith("п.")
                        || map.get("area").toLowerCase().startsWith("пгт.")
                        || map.get("area").toLowerCase().startsWith("д.")) {
                    String val = map.get("area");
//                    LoggingService.writeLog("try get punkt.: " + val, "debug");
                    map.put("prefix_area", val.substring(0, val.indexOf(".")));
                }
                if (map.get("area") != null && map.get("area").toLowerCase().startsWith("г ")
                        || map.get("area").toLowerCase().startsWith("п ")
                        || map.get("area").toLowerCase().startsWith("пгт ")
                        || map.get("area").toLowerCase().startsWith("д ")) {
                    String val = map.get("area");
//                    LoggingService.writeLog("try get punkt\\s: " + val, "debug");
                    map.put("prefix_area", val.substring(0, val.indexOf(" ")));
                }
            }
        }
    }

    private void tryPrefixStreetFromStreet() {
        if (map.get("prefix_sreet").isEmpty()) {
            if (map.get("street") != null && (map.get("street").isEmpty() == false)) {

                if (map.get("street").toLowerCase().startsWith("ул.")
                        || map.get("street").toLowerCase().startsWith("пр.")
                        || map.get("street").toLowerCase().startsWith("пер.")) {
                    String val = map.get("street");
//                    LoggingService.writeLog("try get street.: " + val, "debug");
                    map.put("prefix_sreet", val.substring(0, val.indexOf(".")));
                }
                if (map.get("street").toLowerCase().startsWith("ул ")
                        || map.get("street").toLowerCase().startsWith("пр ")
                        || map.get("street").toLowerCase().startsWith("пер ")) {
                    String val = map.get("street");
//                    LoggingService.writeLog("try get street\\s: " + val, "debug");
                    map.put("prefix_sreet", val.substring(0, val.indexOf(" ")));
                }
            }
        }
    }

}
