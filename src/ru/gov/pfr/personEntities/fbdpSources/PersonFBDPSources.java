package ru.gov.pfr.personEntities.fbdpSources;

import ru.gov.pfr.FNCIDictionaryes.RegionsDictionary;
import ru.gov.pfr.FNCIDictionaryes.RegionsFBDPDictionary;
import ru.gov.pfr.FNCIDictionaryes.SexDictionary;
import ru.gov.pfr.FNCIDictionaryes.StranaDictionary;
import ru.gov.pfr.FNCIDictionaryes.StranaFBDPDictionary;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.utils.DateUtils;
import ru.gov.pfr.xmlStructure.blocs.Benefit;
import ru.gov.pfr.xmlStructure.blocs.FBDPIdentify;

abstract public class PersonFBDPSources extends PersonDataSource {

    protected static String[] keysQueue = new String[]{"NPERS", "ID", "RE", "FA", "IM", "OT", "RDAT", "POL",
        "REG_IND", "REG_RE", "REG_RA", "REG_GOROD", "REG_PUNKT", "REG_UL", "REG_DOM", "REG_KOR", "REG_KVA",
        "FAKT_RE", "FAKT_RA", "FAKT_GOROD", "FAKT_PUNKT", "FAKT_UL", "FAKT_DOM", "FAKT_KOR", "FAKT_KVA",
        "KDOK", "PASS", "PASN", "PAS_DAT", "PAS_KEM",
        "GRAJDAN", "VREG", "L1", "L2", "NSU1", "SROKS", "SROKPO"};
    public static String fatherNamePattern = "^([А-Яа-яёЁ]([ \\.'-]?[А-Яа-яёЁ][\\.]?)*)";
    public static String lastNamePattern = "^[А-Яа-яёЁ]*([А-Яа-яёЁ]+[-\\.\\s\\(\\)]?)+([А-Яа-яёЁ]+$|(?<=[А-Яа-яёЁ])[\\)]?$)";
    public static String namePattern = "^[А-Яа-яёЁ]*([А-Яа-яёЁ]+[-\\.\\s\\(\\)]?)+([А-Яа-яёЁ0-9]+$|(?<=[А-Яа-яёЁ])[\\)]?$)";

    private String controllString;

    protected void parseDictionary() {
        nvpID = map.get("ID");
        checkMonetization();
        makeSex();
        makeCitizenship();
        makeDocument(isNeedCitizenshipConfirmation());
        makeBenefit();
        makeRegion();
    }

    @Override
    public boolean isNeedCitizenshipConfirmation() {
        String citizenship = getCitizenship();
        if (citizenship != null) {
            return !getCitizenship().equals("643");
        }
        return false;
    }

    public void makeCitizenship() {
        String stranaFBDPName = StranaFBDPDictionary.getValue(map.get("GRAJDAN"));
        citizenship = StranaDictionary.getInstance().getValue(stranaFBDPName);
        if (citizenship == null) {
            isValid = false;
            validationMeassage = " Не установлено гражданство ";
        }
    }

    private void checkMonetization() {
        if (map.get("NSU1") != null && map.get("NSU1").isEmpty() == false) {
            if (map.get("NSU1").equals("0")) {// исправлено был вывод монетизации при занчении НЕ 0
                this.isMonetization = true;
            }
        } else {
            this.isMonetization = false;
        }
    }

    public void makeDocument(boolean needCitizenshipConfirmation) {
        map.put("PASS", clearDocumentIDs(map.get("PASS")));
        map.put("PASN", clearDocumentIDs(map.get("PASN")));
        map.put("PAS_DAT", chooseDocumentIssueDate(
                map.get("PAS_DAT"),
                map.get("SROKS"))
        );
        map.put("ISSUE", chooseDocumentIssueDate(
                map.get("PAS_DAT"),
                map.get("SROKS"))
        );
        identify = new FBDPIdentify(
                map.get("KDOK"),
                map.get("PASS"),
                map.get("PASN"),
                map.get("PAS_DAT"),
                map.get("SROKS"),
                needCitizenshipConfirmation);
        if (identify.isValid() == false) {
            isValid = false;
            validationMeassage += identify.getValidationMessage();
        }
    }

    public void makeBenefit() {
        benefit = null;
        String firstL = map.get("L1");
        if (firstL.equals("0") == false) {
            benefit = new Benefit(firstL, map.get("SROKS"), checkMinEndDate(map.get("SROKPO")));

        }
        if (benefit == null || !benefit.isValid()) {
            String secondL = map.get("L2");
            if (secondL != null && secondL.isEmpty() == false && secondL.equals("0") == false) {
                benefit = new Benefit(secondL, map.get("SROKS"), checkMinEndDate(map.get("SROKPO")));
            }
        }
        if (benefit == null) {
            isValid = false;
            validationMeassage += "Льгота не определена";
        } else {
            if (benefit.isValid() == false) {
                isValid = false;
                validationMeassage += benefit.getValidationMessage();
            }
        }
    }

    public void makeRegion() {
        region = RegionsDictionary.getInstance().getValue(map.get("FAKT_RE"));
        if (region == null || region.isEmpty()) {
            region = RegionsDictionary.getInstance().getValue(map.get("REG_RE"));

            if (map.get("RE") != null && map.get("RE").isEmpty() == false) {
                region = RegionsFBDPDictionary.getInstance().getValue(map.get("RE"));
                if (region == null || region.isEmpty()) {
                    isValid = false;
                    validationMeassage += " регион не установлен: "
                            + map.get("FAKT_RE")
                            + " || " + map.get("REG_RE")
                            + " || " + map.get("RE");
                }
            }
        }
    }

    private void makeSex() {
        sex = SexDictionary.getInstance().getValue(map.get("POL"));
        if (sex == null) {
            isValid = false;
            validationMeassage += " пол не установлен ";
        }
    }

    protected void validate() {
        validateFIO();
        isSnilsValid();
    }

    protected void validateFIO() {
        boolean fioValid = true;
        String invalidMessage = "ошибка ФИО: \t";
        replaceDoubleSpaceinFIO();
        String im = map.get("IM");
        if (im.isEmpty() || isEmptyFioPart(im)) {
            map.remove("IM");
        } else {
            if (im.matches(namePattern) == false) {
                fioValid = false;
                invalidMessage += " name: " + map.get("IM");
            }
        }
        String fa = map.get("FA");
        if (fa.isEmpty()) {
            map.remove("FA");
        } else {
            if (fa.matches(lastNamePattern) == false) {
                fioValid = false;
                invalidMessage += " surname: " + map.get("FA");
            }
        }
        String ot = map.get("OT");
        if (ot.isEmpty() || isEmptyFioPart(ot)) {
            map.remove("OT");
        } else {
            if (ot.matches(fatherNamePattern) == false) {
                fioValid = false;
                invalidMessage += " patronymic: " + map.get("OT");
            }
        }
        if (fioValid == false) {
            isValid = false;
            validationMeassage += invalidMessage;
        }
    }

    protected boolean isSnilsValid() {
        String val = map.get("NPERS");
        String[] spaceParts = val.split(" ");
        if (spaceParts.length != 2) {
            isValid = false;
            validationMeassage += " Не валидный СНИЛС ";
            return false;
        }
        String mainPart = spaceParts[0].replace("-", "");
        if (mainPart.matches("\\d{9}") == false) {
            isValid = false;
            validationMeassage += " Не валидный СНИЛС ";
            return false;
        }
        int controlPart = Integer.parseInt(spaceParts[1]);
        int controlAmount = 0;
        byte[] bytes = mainPart.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte[] bb = new byte[1];
            bb[0] = bytes[i];
            controlAmount += Integer.parseInt(new String(bb)) * (bytes.length - i);
        }
        if (controlAmount < 100) {
            if (controlAmount != controlPart) {
                isValid = false;
                validationMeassage += " Не валидный СНИЛС ";
                return false;
            }
        }
        if (controlAmount == 100 || controlAmount == 101) {
            if (spaceParts[1].trim().equals("00") == false) {
                isValid = false;
                validationMeassage += " Не валидный СНИЛС ";
                return false;
            }
        }
        if (controlAmount > 101) {
            if (divideLeft(spaceParts[1], controlPart, controlAmount) == false) {
                isValid = false;
                validationMeassage += " Не валидный СНИЛС ";
                return false;
            }
        }
        return true;
    }

    private boolean divideLeft(String seq, int eq, int val) {
        while (val > 101) {
            val = val % 101;
        }
        if (val < 100) {
            if (val != eq) {
                return false;
            }
        }
        if (val == 100 || val == 101) {
            if (seq.trim().equals("00") == false) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmptyFioPart(String part) {
        if (part.replace("-", "").isEmpty()) {
            return true;
        }
        if (part.toLowerCase().matches("б[\\/.]о")) {
            return true;
        }
        return false;
    }

    private void replaceDoubleSpaceinFIO() {
        map.put("IM", replaceDoubleSpaceAndEndDots(map.get("IM")));
        map.put("FA", replaceDoubleSpaceAndEndDots(map.get("FA")));
        map.put("OT", replaceDoubleSpaceAndEndDots(map.get("OT")));
    }

    private String replaceDoubleSpaceAndEndDots(String s) {
        s = s.trim();
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        while (s.startsWith(" ")) {
            s = s.substring(1);
        }
        while (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        while (s.contains(" .")) {
            s = s.replace(" .", ".");
        }
        while (s.contains(". ")) {
            s = s.replace(". ", ".");
        }
        while (s.contains(" -")) {
            s = s.replace(" -", ".");
        }
        while (s.contains("- ")) {
            s = s.replace("- ", ".");
        }
        while (s.contains(" (")) {
            s = s.replace(" (", "(");
        }
        while (s.contains(" )")) {
            s = s.replace(" )", ")");
        }
        while (s.contains(" .(")) {
            s = s.replace(" .(", "(");
        }
        while (s.contains(" .)")) {
            s = s.replace(" .)", ")");
        }
        return s;
    }

    private String chooseDocumentIssueDate(String docDate, String lgtDate) {
        if (docDate == null || docDate.isEmpty()) {
            return lgtDate;
        }
        return docDate;
    }

    private String clearDocumentIDs(String rawNum) {
        return rawNum.replaceAll("\\s", "").replaceAll(" ", "");
    }

    public String sqlValuesLine(String fileID) {
        this.constructControllString();
        String vals = "("
                + "'" + valueOrEmptyString(controllString) + "',"
                + "'" + valueOrEmptyString(getNPERS()) + "',"
                + "'" + valueOrEmptyString(nvpID) + "',"
                + "'" + valueOrEmptyString(getGuid()) + "',"
                + fileID + ","
                + "0" + ","
                + "'" + valueOrEmptyString(map.get("FA")) + "',"
                + "'" + valueOrEmptyString(map.get("IM")) + "',"
                + "'" + valueOrEmptyString(map.get("OT")) + "',"
                + "'" + valueOrEmptyString(map.get("RDAT")) + "',"
                + "'" + valueOrEmptyString(sex) + "',"
                + "'" + valueOrEmptyString(citizenship) + "',"
                + "'" + valueOrEmptyString(identify.getDocType()) + "',"
                + "'" + valueOrEmptyString(identify.getSerial()) + "',"
                + "'" + valueOrEmptyString(identify.getNumber()) + "',"
                + "'" + valueOrEmptyString(identify.getIssue()) + "',"
                + "'" + valueOrEmptyString(region) + "',"
                + "'" + valueOrEmptyString(benefit.getExtendedCode()) + "',";
        if (isMonetization) {
            vals += "'" + valueOrEmptyString(benefit.getStartDate()) + "',"
                    + "'" + valueOrEmptyString(benefit.getEndDate()) + "',"
                    + valueOrEmptyString(map.get("NSU1")) + "," //nsu
                    + "0,"//mfileid
                    + "0" //ismloaded
                    ;
        } else {
            vals += "'" + valueOrEmptyString(benefit.getMonetizationStartDate()) + "',"
                    + "'" + valueOrEmptyString(benefit.getMonetizationEndDate()) + "',"
                    + valueOrEmptyString(map.get("NSU1")) + "," //nsu
                    + "0,"//mfileid
                    + "0" //ismloaded
                    ;
        }
        vals += ")";
        return vals;
    }

    public void constructControllString() {
        String hash = map.get("surname") + map.get("name") + map.get("patronymic") + map.get("sex")
                + map.get("citizenship")
                + map.get("snils")
                + map.get("doc_type") + map.get("serial") + map.get("num") + map.get("date_issue")
                + map.get("region")
                + map.get("ext_benefit_code") + map.get("receive_date") + map.get("cancel_date");
        controllString = getNPERS() + hash.hashCode();
    }

    private static String valueOrEmptyString(String val) {
        if (val != null) {
            return val;
        }
        return "";
    }

    @Override
    public boolean isDBWriteble() {
        return true;
    }

    private String checkMinEndDate(String realDateValue) {
        String settingsgMinDateValue = SettingsService.getInstance().getValue("minimum_lgota_cancel_date");
        if (!settingsgMinDateValue.isEmpty() && !realDateValue.isEmpty()) {
            return DateUtils.thresholdMinDateOrIncomingDate(settingsgMinDateValue, realDateValue);
        }
        return realDateValue;
    }

}
