package ru.gov.pfr.FNCIDictionaryes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author kneretin
 */
public class RegionsDictionary {

    private static File dictFile;
    private static RegionsDictionary instance;
    private static HashMap<String, String> map = new HashMap<>();
    private static HashMap<String, String> reverseMap = new HashMap<>();

    public RegionsDictionary() {
        String here = new File(".").getAbsolutePath();
        File folder = new File(here.substring(0, here.indexOf(".")));
        dictFile = new File(folder.getAbsolutePath() + "\\dict\\doc\\regionOkato5.csv");
    }

    private static void readDict(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        String string;
        while ((string = reader.readLine()) != null) {
            string = string.toLowerCase();
            if (string.contains(";")) {
                String[] vals = string.split(";");
                if (vals.length == 2) {
                    map.put(vals[0].trim(), vals[1].trim());
                } else {
                    map.put(vals[0].trim(), "");

                }
            }
        }
        reader.close();
    }

    private static void makeReverseMap() {
        for (String key : map.keySet()) {
            reverseMap.put(map.get(key), key);
        }
    }

    public static RegionsDictionary getInstance() {
        if (instance == null) {
            RegionsDictionary dnd = new RegionsDictionary();
            try {
                readDict(dictFile);
                makeReverseMap();
                instance = dnd;
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
        return instance;
    }

    public static void addValue(String key, String value) {
        getInstance().map.put(key, value);
    }

    public static String getValue(String key) {
        key = key.trim().toLowerCase();
        if (getInstance().map.containsKey(key) == false) {
            if (key.toLowerCase().contains("москва")) {
                return "45000";
            }
            if (key.toLowerCase().contains("краснодар")) {
                return "03000";
            }
            if (key.toLowerCase().contains("волгоград")) {
                return "18000";
            }
            if (key.toLowerCase().contains("северная осетия")) {
                return "90000";
            }
            if (key.toLowerCase().contains("башкортостан")) {
                return "80000";
            }
            if (key.toLowerCase().contains("г пермь")) {
                return "57000";
            }
            if (key.toLowerCase().contains("калужская")) {
                return "29000";
            }
            if (key.toLowerCase().contains("хабаровск")) {
                return "08000";
            }
            if (key.toLowerCase().contains("смоленск")) {
                return "66000";
            }
            if (key.toLowerCase().contains("нижний новгород")) {
                return "66000";
            }
            if (key.toLowerCase().contains("якутия")) {
                return "98000";
            }
            if (key.toLowerCase().contains("бурятия")) {
                return "81000";
            }
            if (key.toLowerCase().contains("новосибирск")) {
                return "50000";
            }
            if (key.toLowerCase().contains("псков")) {
                return "58000";
            }
            if (key.toLowerCase().contains("сахалинск")) {
                return "64000";
            }
            if (key.toLowerCase().contains("тверь")) {
                return "28000";
            }
            if (key.toLowerCase().contains("ярослав")) {
                return "78000";
            }
            if (key.toLowerCase().contains("магадан")) {
                return "44000";
            }
            if (key.toLowerCase().contains("калуга")) {
                return "29000";
            }
            if (key.toLowerCase().contains("воронеж")) {
                return "20000";
            }
            if (key.toLowerCase().contains("тула")) {
                return "70000";
            }
            if (key.toLowerCase().contains("челябинск")) {
                return "75000";
            }
            if (key.toLowerCase().contains("владивосток")) {
                return "05000";
            }
            if (key.toLowerCase().contains("владикавказ")) {
                return "90000";
            }
            if (key.toLowerCase().contains("алания")) {
                return "90000";
            }
            if (key.toLowerCase().contains("чукотс")) {
                return "77000";
            }
            if (key.toLowerCase().contains("улан-удэ")) {
                return "04000";
            }
            if (key.toLowerCase().contains("астрахань")) {
                return "12000";
            }
            if (key.toLowerCase().contains("орел")) {
                return "54000";
            }
            if (key.toLowerCase().contains("коми")) {
                return "87000";
            }
            if (key.toLowerCase().contains("зеленоград")) {
                return "45000";
            }
            if (key.toLowerCase().contains("пенза")) {
                return "56000";
            }
            if (key.toLowerCase().contains("барнаул")) {
                return "01000";
            }
            if (key.toLowerCase().contains("анадырь")) {
                return "77000";
            }
            if (key.toLowerCase().contains("ростов-на-дону")) {
                return "60000";
            }
            if (key.toLowerCase().contains("королев")) {
                return "46000";
            }
            if (key.toLowerCase().contains("протвино")) {
                return "46000";
            }
            if (key.toLowerCase().contains("дедовск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("щербинка")) {
                return "46000";
            }
            if (key.toLowerCase().contains("фрязино")) {
                return "46000";
            }
            if (key.toLowerCase().contains("крекшино")) {
                return "46000";
            }
            if (key.toLowerCase().contains("кокошкино")) {
                return "46000";
            }
            if (key.toLowerCase().contains("михайлово-ярцевское")) {
                return "46000";
            }
            if (key.toLowerCase().contains("щаповское")) {
                return "46000";
            }
            if (key.toLowerCase().contains("московская")) {
                return "46000";
            }
            if (key.toLowerCase().contains("краснопахорское")) {
                return "46000";
            }
            if (key.toLowerCase().contains("лобня")) {
                return "46000";
            }
            if (key.toLowerCase().contains("руза")) {
                return "46000";
            }
            if (key.toLowerCase().contains("красногорск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("люберцы")) {
                return "46000";
            }
            if (key.toLowerCase().contains("котельники")) {
                return "46000";
            }
            if (key.toLowerCase().contains("звенигород")) {
                return "46000";
            }
            if (key.toLowerCase().contains("климовск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("химки")) {
                return "46000";
            }
            if (key.toLowerCase().contains("долгопрудный")) {
                return "46000";
            }
            if (key.toLowerCase().contains("мытищи")) {
                return "46000";
            }
            if (key.toLowerCase().contains("коломна")) {
                return "46000";
            }
            if (key.toLowerCase().contains("кашира")) {
                return "46000";
            }
            if (key.toLowerCase().contains("одинцово")) {
                return "46000";
            }
            if (key.toLowerCase().contains("лыткарино")) {
                return "46000";
            }
            if (key.toLowerCase().contains("краснознаменск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("орехово-зуево")) {
                return "46000";
            }
            if (key.toLowerCase().contains("сергиев посад")) {
                return "46000";
            }
            if (key.toLowerCase().contains("дмитров")) {
                return "46000";
            }
            if (key.toLowerCase().contains("железня")) {
                return "46000";
            }
            if (key.toLowerCase().contains("краснозаводск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("дубна")) {
                return "46000";
            }
            if (key.toLowerCase().contains("корыстово")) {
                return "46000";
            }
            if (key.toLowerCase().contains("дзержинский")) {
                return "46000";
            }
            if (key.toLowerCase().contains("троицк")) {
                return "46000";
            }
            if (key.toLowerCase().contains("куровское")) {
                return "46000";
            }
            if (key.toLowerCase().contains("хотьково")) {
                return "46000";
            }
            if (key.toLowerCase().contains("электрогорск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("солнечногорск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("павловский посад")) {
                return "46000";
            }
            if (key.toLowerCase().contains("воскресенск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("раменское")) {
                return "46000";
            }
            if (key.toLowerCase().contains("сходня")) {
                return "46000";
            }
            if (key.toLowerCase().contains("наро-фоминск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("г.чехов")) {
                return "46000";
            }
            if (key.toLowerCase().contains("серпухов")) {
                return "46000";
            }
            if (key.toLowerCase().contains("г.клин")) {
                return "46000";
            }
            if (key.toLowerCase().contains("клин")) {
                return "46000";
            }
            if (key.toLowerCase().contains("фирсановка")) {
                return "46000";
            }
            if (key.toLowerCase().contains("талдом")) {
                return "46000";
            }
            if (key.toLowerCase().contains("бронницы")) {
                return "46000";
            }
            if (key.toLowerCase().contains("электросталь")) {
                return "46000";
            }
            if (key.toLowerCase().contains("балашиха")) {
                return "46000";
            }
            if (key.toLowerCase().contains("птичное")) {
                return "46000";
            }
            if (key.toLowerCase().contains("серебряные пруды")) {
                return "46000";
            }
            if (key.toLowerCase().contains("подольск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("волоколамск")) {
                return "46000";
            }
            if (key.toLowerCase().contains("луховицы")) {
                return "46000";
            }
            if (key.toLowerCase().contains("тамбов")) {
                return "68000";
            }
            if (key.toLowerCase().contains("рязань")) {
                return "61000";
            }
            if (key.toLowerCase().contains("мурманск")) {
                return "47000";
            }
            if (key.toLowerCase().contains("чувашия")) {
                return "97000";
            }
            if (key.toLowerCase().contains("корякск")) {
                return "30000";
            }
            if (key.toLowerCase().contains("мордовия")) {
                return "89000";
            }
            if (key.toLowerCase().contains("брянск")) {
                return "15000";
            }
            if (key.toLowerCase().contains("новгород")) {
                return "49000";
            }
            if (key.toLowerCase().contains("якутск")) {
                return "98000";
            }
            if (key.toLowerCase().contains("дагестан")) {
                return "82000";
            }
            if (key.toLowerCase().contains("крым")) {
                return "35000";
            }
            if (key.toLowerCase().contains("уфа")) {
                return "80000";
            }
            if (key.toLowerCase().contains("иваново")) {
                return "24000";
            }
            if (key.toLowerCase().contains("вороновское")) {
                return "34000";
            }
            if(key.toLowerCase().contains("херсон")){
                return "74000";
            }
            if(key.toLowerCase().contains("луганск")){
                return "43000";
            }
            if(key.toLowerCase().contains("донецк")){
                return "21000";
            }
            if(key.toLowerCase().contains("запорожск")){
                return "23000";
            }

            if (key.contains("г ")) {
                key = key.replace("г ", "");
                return getValue(key);
            }
            if (key.contains("область") == false) {
                String newkey = key + " область";
                if (getInstance().map.containsKey(newkey.trim())) {
                    return getValue(newkey);
                }
            }
            if (key.contains("обл") && key.contains("область") == false) {
                if (key.contains(".")) {
                    key = key.replace(".", "");
                }
                key = key.replace("обл", "область");
                return getValue(key);
            }

            if (key.startsWith("область")) {
                String newkey = key.replace("область", "").trim() + " область";
                if (getInstance().map.containsKey(newkey.trim())) {
                    return getValue(newkey);
                }
            }
            if (key.contains("респ") && key.contains("республика") == false) {
                key = key.replace("респ", "республика");
                if (getInstance().map.containsKey(key) == false) {
                    if (key.startsWith("республика")) {
                        key = key.replace("республика", "").trim() + " республика";
                        if (getInstance().map.containsKey(key) == false) {
                            if (key.endsWith("республика")) {
                                key = "республика " + key.replace("республика", "").trim();
                                if (getInstance().map.containsKey(key) == false) {
                                    return null;
                                } else {
                                    return getValue(key);
                                }
                            }
                        } else {
                            return getValue(key);
                        }
                    } else {
                        String newKey = "республика " + key.replace("республика", "").trim();
                        if (getInstance().map.containsKey(newKey) == false) {
                            LoggingService.writeLog("no republic: " + newKey + " | " + key, "error");
                        }
                        return getValue(key);
                    }
                }
                return getValue(key);
            }
        }
        return getInstance().map.get(key);
    }

    public static void print() {
        LoggingService.writeLog("--RegionsDictionary--", "debug");
        for (String key : getInstance().map.keySet()) {
            LoggingService.writeLog("key: " + key + " - " + getInstance().map.get(key), "debug");
        }
    }

    public static HashMap<String, String> getMap() {
        return getInstance().map;
    }

    public static String getRegionNameByCode(String code) {
        RegionsDictionary.getInstance();
        if (reverseMap.containsKey(code)) {
            return reverseMap.get(code);
        }
        return null;
    }
}
