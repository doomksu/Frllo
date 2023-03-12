package ru.gov.pfr.personEntities.frlloSources;

import java.util.LinkedHashMap;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.xmlStructure.blocs.Benefit;
import ru.gov.pfr.xmlStructure.blocs.FRLLOIdentify;

public class PersonFRLLOSourceData extends PersonDataSource {

    private Integer fileId;

    public PersonFRLLOSourceData(LinkedHashMap<String, String> convertedMap) {
        String replaceTemplate = "&QUOT;";
        for (String string : convertedMap.values()) {
            string = string.replaceAll(replaceTemplate, "");
        }
        for (String key : convertedMap.keySet()) {
            map.put(key, convertedMap.get(key));
        }
        citizenship = map.get("CITIZENSHIP");
        fileId = Integer.parseInt(map.get("FILE_ID"));
        boolean isRussian = !isNeedCitizenshipConfirmation();
        identify = new FRLLOIdentify(map.get("DOCTYPE"), map.get("SERIAL"), map.get("DOCNUMBER"), map.get("ISSUE"), null, isRussian);
        benefit = new Benefit(map.get("BENEFIT"), map.get("RECEIVE_DATE"), map.get("CANCEL_DATE"));
        region = map.get("REGION");
        if (region != null) {
            if (region.equals("3000")) {
                region = "03000";
            }
        }
        nvpID = map.get("ID_NVP");
        sex = map.get("SEX");
        guid = map.get("GUID");
        if (!identify.isValid() || !benefit.isValid()) {
            isValid = false;
        }
    }

    @Override
    public String getName() {
        return map.get("IM");
    }

    @Override
    public String getSurname() {
        return map.get("FA");
    }

    @Override
    public String getPatronymic() {
        return map.get("OT");
    }

    @Override
    public String getBirthdate() {
        return map.get("RDAT");
    }

    @Override
    public String getWritableSnils() {
        return map.get("NPERS").replaceAll(" ", "").replaceAll("-", "");
    }

    @Override
    public boolean isNeedCitizenshipConfirmation() {
        if (citizenship != null) {
            return !getCitizenship().equals("643");
        }
        return false;
    }

    @Override
    public boolean isDBWriteble() {
        return false;
    }

    public Integer getFileId() {
        return fileId;
    }

}
