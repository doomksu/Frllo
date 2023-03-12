package ru.gov.pfr.xmlStructure.blocs;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.utils.DateUtils;
import ru.gov.pfr.xmlStructure.MapXMLContainer;

public class Benefit extends MapXMLContainer {

    private boolean isValid = true;
    private String validationMessage = "";

    public Benefit(String code, String sroks, String srokpo) {
        init();
        if (code.isEmpty() == false) {
            while (code.length() < 3) {
                code = "0" + code;
            }
            map.put("ext_benefit_code", code);
        }
        map.put("receive_date", sroks);
        map.put("cancel_date", srokpo);
        innerValidation();
    }

    private void innerValidation() {
        String srokpoVal = map.get("cancel_date");
        boolean cancelDateSeted = false;
        if (srokpoVal != null) {
            if (srokpoVal.isEmpty() == false && srokpoVal.contains("9999") == false) {// убираем закрытие с 9999 
                map.put("cancel_date", srokpoVal);
                cancelDateSeted = true;
            }
        }
        if (!cancelDateSeted) {
            map.remove("cancel_date");
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
        if (map.get("receive_date") == null) {
            isValid = false;
            validationMessage += "дата установления: NULL";
            return;
        }
        if (map.get("receive_date").matches("\\d{4}-\\d{2}-\\d{2}") == false) {
            isValid = false;
            validationMessage += "ошибка даты установления: " + (map.get("receive_date").isEmpty() ? "пустая строка " : map.get("receive_date"));
        }
        if (map.get("cancel_date") != null) {
            if (map.get("cancel_date").matches("\\d{4}-\\d{2}-\\d{2}") == false) {
                isValid = false;
                validationMessage += "ошибка даты прекращения: " + (map.get("cancel_date").isEmpty() ? "пустая строка " : map.get("cancel_date"));
            }
        }
    }

    public String getFBDPKod() {
        return map.get("ext_benefit_code");
    }

    private void init() {
        wrapperTeg = "benefit";
        keysQueue = new String[]{"benefit_code", "ext_benefit_code",
            "receive_date", "cancel_date"};
        map = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder val = new StringBuilder();
        val.append(super.toString());
        return val.toString();
    }

    public boolean isValid() {
        return isValid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public String getMonetizationStartDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.datePattern);
        LocalDateTime now = LocalDateTime.now();
        String startDate = map.get("receive_date");
        String[] dateParts = startDate.split("-");
        if (dateParts.length == 3) {
            try {
                if (sdf.parse(startDate).getYear() < now.getYear()) {
                    int imonth = Integer.parseInt(dateParts[1]);
                    int idate = Integer.parseInt(dateParts[2]);
                    if (imonth <= 10 || (imonth == 10 && idate == 1)) {
                        startDate = now.getYear() + "-01-01";
                    }
                }
            } catch (Exception ex) {
                LoggingService.writeLog("error parse dates", "error");
            }
        }
        return startDate;
    }

    public String getMonetizationEndDate() {
        String endDate = "";
        if (map.get("cancel_date") == null || map.get("cancel_date").isEmpty()) {
            endDate = DateUtils.getEndOfYearDateString();
        } else {
            endDate = map.get("cancel_date");
        }
        String[] sParts = endDate.split("-");
        LocalDate endLocalDate = LocalDate.of(
                Integer.parseInt(sParts[0]),
                Integer.parseInt(sParts[1]),
                Integer.parseInt(sParts[2]));

        sParts = getMonetizationStartDate().split("-");
        LocalDate startLocalDate = LocalDate.of(
                Integer.parseInt(sParts[0]),
                Integer.parseInt(sParts[1]),
                Integer.parseInt(sParts[2]));

        if (startLocalDate.isAfter(endLocalDate)) {
            endDate = startLocalDate.format(DateTimeFormatter.ofPattern(DateUtils.datePattern));
        }

        if (endDate.isEmpty()) {
            isValid = false;
            validationMessage = "Пустая дата окончания монтизации";
        }
        return endDate;
    }

    public String getExtendedCode() {
        return map.get("ext_benefit_code");
    }

    public String getStartDate() {
        return map.get("receive_date");
    }

    public String getEndDate() {
        return map.get("cancel_date");
    }

    public void closeMonetizationToday() {
        String start = getMonetizationStartDate();
        String[] sParts = start.split("-");
        LocalDate ld = LocalDate.of(
                Integer.parseInt(sParts[0]),
                Integer.parseInt(sParts[1]),
                Integer.parseInt(sParts[2]));
        LocalDateTime now = LocalDateTime.now();
        if (ld.getYear() < now.getYear()) {
            map.put("cancel_date", "" + now.getYear() + "-01-01");
        } else {
            if (ld.getYear() == now.getYear()) {
                map.put("cancel_date", start);
            }
        }
    }

}
