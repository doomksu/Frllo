package ru.gov.pfr.xmlStructure.blocs;

import java.util.HashMap;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.xmlStructure.MapXMLContainer;

public abstract class CitizenshipIdentify extends MapXMLContainer {

    protected String doctypeOriginal = "";
    protected String validationMessage = "";
    protected boolean isRussian = true;
    protected String additionalDate;

    public CitizenshipIdentify(String docType, String serial, String number, String dateIssue, String additionalDateIssue, boolean needCitizenshipConfirmation) {
        init();
        doctypeOriginal = docType;
        calculateFields(docType, serial, number, dateIssue, additionalDateIssue);
        map.put("authority", "");
        if (map.get("serial") == null || map.get("serial").isEmpty()) {
            map.remove("serial");
        }
        String[] keys = new String[map.size()];
        int i = 0;
        for (String string : keysQueue) {
            if (map.containsKey(string)) {
                keys[i] = string;
                i++;
            }
        }
        keysQueue = keys;
        this.isRussian = !needCitizenshipConfirmation;
    }

    protected void init() {
        wrapperTeg = "doc";
        keysQueue = new String[]{"doc_type", "serial", "num", "date_issue", "authority"};
        map = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder val = new StringBuilder();
        val.append("<identify_docs>\r\n");
        val.append(super.toString());
        val.append("</identify_docs>\r\n");
        return val.toString();
    }

    public String getUnwrapedContent() {
        StringBuilder val = new StringBuilder();
        val.append(super.toString());
        return val.toString();
    }

    public String toCitizenConfirmString() {
        StringBuilder val = new StringBuilder();
        val.append(super.toString());
        return val.toString();
    }

    public boolean isValid() {
        String docType = map.get("doc_type");
        String serial = map.get("serial");
        if (serial != null) {
            serial = serial.replace(" ", "");
        } else {
            return false;
        }
        String number = map.get("num");;
        if (number != null) {
            number = number.replace(" ", "");
        } else {
            return false;
        }

        boolean valid = true;
        if (map.get("date_issue") == null || map.get("date_issue").isEmpty()) {
            validationMessage += " Неустановленная или пустая дата выдачи документа";
            valid = false;
            return false;
        }
        if (docType == null || docType.isEmpty()) {
            validationMessage += " Неустановленный вид документа: " + map.get("KDOK") + " | " + map.get("doc_type");
            valid = false;
        } else {
            if (docType.equals("1.999")) {//Паспорт СССР
                String val = SettingsService.getInstance().getValue("ussrPasportAsDifferent");
                boolean ussrPassportAsDifferent = Boolean.parseBoolean(val);
                if (ussrPassportAsDifferent) {
                    map.put("doc_type", "1.999");   //инные документы
                    String docName = map.get("KDOK");
                    if (docName.toLowerCase().equals("паспорт")) {
                        docName = "ПАСПОРТ СССР";
                    }
                    if (docName.toLowerCase().equals("згпаспорт")) {
                        docName = "ЗАГРАНПАСПОРТ СССР";
                    }
                    if (!isRussian) {
                        map.put("doc_type_name", docName);   //инные документы - имя
                        keysQueue = new String[]{"doc_type", "doc_type_name", "serial", "num", "date_issue", "authority"};
                    } else {
                        keysQueue = new String[]{"doc_type", "serial", "num", "date_issue", "authority"};
                    }
                    if (serial != null && number != null) {
                        if (serial.isEmpty() || serial.length() > 20
                                || number.length() > 20 || number.isEmpty()) {
                            validationMessage += "Невалидная серия или номер иного документа" + serial + " " + number;
                            valid = false;
                        }
                    }
                } else {
                    validationMessage += " Паспорт СССР\t ser-" + serial + " number-" + number;
                    valid = false;
                }
            }
            if (docType.equals("6")) {//Свидетельство о рождении
                if (!Boolean.logicalAnd(
                        serial.matches("^[IVXLC1УХЛС]{1,4}-[А-Я]{2}"),
                        number.matches("\\d{6}"))) {

                    String val = SettingsService.getInstance().getValue("foreignBirthDocument");
                    boolean useForeignDoc = Boolean.parseBoolean(val);
                    if (useForeignDoc) {
                        map.put("doc_type", "32");   //св-во о рожд. иного госсударстваcl
                    } else {
                        validationMessage += " Свидетельство о рождении\t ser-" + serial + " number-" + number;
                        valid = false;
                    }
                }
            }
            if (docType.equals("9")) {//Военный билет
                if (!Boolean.logicalAnd(
                        serial.matches("^[А-Я]{2}"),
                        number.matches("\\d{6,7}"))) {
                    validationMessage += " Военный билет\t ser-" + serial + " number-" + number;
                    valid = false;
                }
            }
            if (docType.equals("5")) {//Временное удостоверение личности
                if (number.length() > 14 || number.length() == 0) {
                    validationMessage += " Временное удостоверение личности\t ser-" + serial + " number-" + number;
                    valid = false;
                }
            }
            if (docType.equals("1")) {//Паспорт гражданина РФ
                if (!Boolean.logicalAnd(
                        serial.matches("^\\d{4}"),
                        number.matches("\\d{6}"))) {
                    validationMessage += " Паспорт\t ser-" + serial + " number-" + number;
                    valid = false;
                }
            }
            if (docType.equals("2")) {//Загранпаспорт  гражданина РФ
                if (!Boolean.logicalAnd(
                        serial.matches("^\\d{2}"),
                        number.matches("\\d{7}"))) {
                    validationMessage += " Загранпаспорт ser-" + serial + " number-" + number;
                    valid = false;
                }
            }
        }
        return valid;
    }

    public boolean isWritable(String snils) {
        if (map.get("doc_type").equals("1.999") && snils != null && snils.isEmpty() == false) {
            return false;
        }
        return true;
    }

    public String getValidationMessage() {
        if (!isValid()) {
            return "ошибка документа: " + validationMessage;
        }
        return "false alarm in identify";
    }

    protected abstract void calculateFields(String docType, String serial, String number, String dateIssue, String additionalDateIssue);

    public String getDocType() {
        return map.get("doc_type");
    }

    public String getSerial() {
        return map.get("serial");
    }

    public String getNumber() {
        return map.get("num");
    }

    public String getIssue() {
        return map.get("date_issue");
    }

    public String getAdditionalDate() {
        return additionalDate;
    }

    public String getDoctypeOriginal() {
        return doctypeOriginal;
    }

    public void dropDocTypeName() {
        map.remove("doc_type_name");
        keysQueue = new String[]{"doc_type", "serial", "num", "date_issue", "authority"};
    }

}
