package ru.gov.pfr.personEntities;

import ru.gov.pfr.FNCIDictionaryes.XMLToControllComparisonDictionary;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.utils.DateUtils;
import ru.gov.pfr.xmlStructure.blocs.Benefit;
import ru.gov.pfr.xmlStructure.blocs.CitizenshipIdentify;

public abstract class PersonDataSource {

    protected HashMap<String, String> map = new HashMap<>();
    protected boolean modifyWithDictionaryes = false;
    protected boolean isValid = true;
    protected String validationMeassage = "";

    protected String sex;
    protected String citizenship;
    protected CitizenshipIdentify identify;
    protected Benefit benefit;
    protected String region;
    protected String guid;
    protected boolean isMonetization = false;
    protected String nvpID;
    private static final String LOG_CHANGES_DIFFERENCE = "LOG_CHANGES_DIFFERENCE";
    private static final String LOG_CHANGES_EQUALITY = "LOG_CHANGES_EQUALITY";

    public void print() {
        LoggingService.writeLog("PersonDataSource print:", "debug");
        for (String key : map.keySet()) {
            LoggingService.writeLog("key: " + key + " : " + map.get(key), "debug");
        }
    }

    public String getValue(String key) {
        return map.get(key);
    }

    protected void makeNewGUID() {
        guid = UUID.randomUUID().toString();
    }

    public String getGuid() {
        if (guid == null) {
            makeNewGUID();
        }
        return guid;
    }

    public String getDateString() {
        Format format3 = new SimpleDateFormat(DateUtils.dateTimePattern);
        String dateString = format3.format(Calendar.getInstance().getTime());
        return dateString;
    }

    public String getName() {
        return map.get("IM");
    }

    public String getSurname() {
        return map.get("FA");
    }

    public String getPatronymic() {
        return map.get("OT");
    }

    public String getBirthdate() {
        return map.get("RDAT");
    }

    public String getNPERS() {
        return map.get("NPERS");
    }

    public String getWritableSnils() {
        return map.get("NPERS").replaceAll(" ", "").replaceAll("-", "");
    }

    abstract public boolean isNeedCitizenshipConfirmation();

    public String getCitizenship() {
        return citizenship;
    }

    public CitizenshipIdentify getDocument() {
        return identify;
    }

    public Benefit getBenefit() {
        return benefit;
    }

    public String getRegion() {
        return region;
    }

    public String getSex() {
        return sex;
    }

    public String getNVPID() {
        return nvpID;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getValidationMeassage() {
        return validationMeassage;
    }

    public boolean isMonetization() {
        return isMonetization;
    }

    public PersonXMLStructure makeXMLStructure() {
        return new PersonXMLStructure(this);
    }

    public boolean isChanged(PersonDataSource fbdpNewPerson) {
        HashMap<String, String> controllMap = mapToLowerCaseKeys(map);
        HashMap<String, String> fbdpXMLMap = mapToLowerCaseKeys(fbdpNewPerson.map);
        HashMap<String, String> connectionMap = XMLToControllComparisonDictionary.getInstance().getMap();

        boolean changed = false;
        for (Map.Entry<String, String> entry : connectionMap.entrySet()) {
            String xmlKey = entry.getKey();
            String controllKey = entry.getValue();
            if (fbdpXMLMap.containsKey(xmlKey) && controllMap.containsKey(controllKey)) {
                if (!fbdpXMLMap.get(xmlKey).equals(controllMap.get(controllKey))) {
                    changed = true;
                    LoggingService.writeLogIfDummy(LOG_CHANGES_DIFFERENCE, ">> difference:" + xmlKey + "-" + controllKey + "\txml: " + fbdpXMLMap.get(xmlKey) + "\tcontroll: " + controllMap.get(controllKey), "debug");
                } else {
                    LoggingService.writeLogIfDummy(LOG_CHANGES_EQUALITY, ">> equality:" + xmlKey + "-" + controllKey + " xml: " + fbdpXMLMap.get(xmlKey) + "\tcontroll: " + controllMap.get(controllKey), "debug");
                }
            }
        }
        return changed;
    }

    public boolean isDBWriteble() {
        return false;
    }

    public String sqlValuesLine(String fileID) {
        return "";
    }

    public void closeMonetization() {
        if (benefit != null) {
            benefit.closeMonetizationToday();
        }
    }

    public boolean isOpened() {
        if (map.containsKey("OPER")) {
            String oper = map.get("OPER");
            if (oper.toLowerCase().contains("пре") || oper.toLowerCase().contains("сня")) {
                return false;
            }
            if (map.containsKey("DSM")) {
                if (map.get("DSM") != null && !map.get("DSM").isEmpty()) {
                    return false;
                }
            }

        }
        return true;
    }

    private HashMap<String, String> mapToLowerCaseKeys(HashMap<String, String> in) {
        HashMap<String, String> out = new HashMap<>();
        for (Map.Entry<String, String> entry : in.entrySet()) {
            out.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return out;
    }
}
