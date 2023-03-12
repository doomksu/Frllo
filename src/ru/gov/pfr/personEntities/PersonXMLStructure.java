package ru.gov.pfr.personEntities;

import ru.gov.pfr.xmlStructure.blocs.Address;
import ru.gov.pfr.xmlStructure.blocs.Benefit;
import ru.gov.pfr.xmlStructure.blocs.CitizenshipIdentify;
import ru.gov.pfr.xmlStructure.blocs.DocumentConfirmation;

public class PersonXMLStructure {

    protected CitizenshipIdentify document;
    protected Benefit benefit;
    protected Benefit benefit2;
    protected Address address;
    private static final String BLOCK = "document";

    protected boolean isValid = true;
    protected String validationMeassage = "";
    private final PersonDataSource dataSource;

    public PersonXMLStructure(PersonDataSource pds) {
        this.dataSource = pds;
        document = dataSource.getDocument();
        benefit = dataSource.getBenefit();
    }

    public String makeLgotaXML() {
        StringBuilder comp = new StringBuilder();
        comp.append("<" + BLOCK + ">\r\n");
        comp.append("<document_id>" + dataSource.getGuid() + "</document_id>\r\n");
        comp.append("<doc_date_time>" + dataSource.getDateString() + "</doc_date_time>\r\n");
        comp.append("<citizen>\r\n");
        comp.append("<register_id/>\r\n");
        comp.append("<ext_citizen_id>" + dataSource.getNVPID() + "</ext_citizen_id>\r\n");
        appendOrSkip(comp, dataSource.getName(), "name");
        appendOrSkip(comp, dataSource.getSurname(), "surname");
        appendOrSkip(comp, dataSource.getPatronymic(), "patronymic");

        comp.append("<birthdate>" + dataSource.getBirthdate() + "</birthdate>\r\n");
        comp.append("<sex>" + dataSource.getSex() + "</sex>\r\n");
        comp.append("<citizenship>" + dataSource.getCitizenship() + "</citizenship>\r\n");
        if (dataSource.isNeedCitizenshipConfirmation()) {
            comp.append("<citizenship_confirm>\r\n");
            DocumentConfirmation dc = new DocumentConfirmation(
                    document.getDocType(),
                    document.getSerial(),
                    document.getNumber(),
                    document.getIssue(),
                    document.getAdditionalDate(),
                    false);
            dc.setDocument(document);
            comp.append(dc.getUnwrapedContent());
            comp.append("</citizenship_confirm>\r\n");
            document.dropDocTypeName(); //если уже есть блок документов - подтверждение гражданства \ право на пенсию в РФ
        }
        comp.append("<snils>" + dataSource.getWritableSnils() + "</snils>\r\n");
        comp.append(document.toString());
        comp.append("<region>" + dataSource.getRegion() + "</region>\r\n");
        comp.append("</citizen>\r\n");
        comp.append("<benefits>\r\n");
        comp.append(benefit.toString());
        comp.append("</benefits>\r\n");

        comp.append("</" + BLOCK + ">\r\n");
        return comp.toString();
    }

    public String makeMonetizationXML() {
        StringBuilder comp = new StringBuilder();
        comp.append("<" + BLOCK + ">\r\n");
        comp.append("<document_id>" + dataSource.getGuid() + "</document_id>\r\n");
        comp.append("<doc_date_time>" + dataSource.getDateString() + "</doc_date_time>\r\n");
        comp.append("<citizen>\r\n");
        comp.append("<register_id/>\r\n");
        comp.append("<ext_citizen_id>" + dataSource.getNVPID() + "</ext_citizen_id>\r\n");
        appendOrSkip(comp, dataSource.getName(), "name");
        appendOrSkip(comp, dataSource.getSurname(), "surname");
        appendOrSkip(comp, dataSource.getPatronymic(), "patronymic");
        comp.append("<birthdate>" + dataSource.getBirthdate() + "</birthdate>\r\n");
        comp.append("<sex>" + dataSource.getSex() + "</sex>\r\n");
        comp.append("<snils>" + dataSource.getWritableSnils() + "</snils>\r\n");
        comp.append(document.toString());
        comp.append("<region>" + dataSource.getRegion() + "</region>\r\n");
        comp.append("</citizen>\r\n");
        comp.append("<monetizations>\r\n");
        comp.append("<monetization>\r\n");
        if (benefit.getMonetizationStartDate() != null && !benefit.getMonetizationStartDate().isEmpty()) {
            comp.append("<start_date>" + benefit.getMonetizationStartDate() + "</start_date>\r\n");
        }
        if (benefit.getMonetizationEndDate() != null && !benefit.getMonetizationEndDate().isEmpty()) {
            comp.append("<end_date>" + benefit.getMonetizationEndDate() + "</end_date>\r\n");
        }
        comp.append("</monetization>\r\n");
        comp.append("</monetizations>\r\n");
        comp.append("</" + BLOCK + ">\r\n");
        return comp.toString();
    }

    private void appendOrSkip(StringBuilder comp, String val, String teg) {
        if (val != null && val.isEmpty() == false) {
            comp.append("<" + teg + ">" + val + "</" + teg + ">\r\n");
        }
    }
}
