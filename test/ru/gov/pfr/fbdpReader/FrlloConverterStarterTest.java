package ru.gov.pfr.fbdpReader;

import java.util.LinkedHashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.controller.MockMainController;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author User
 */
public class FrlloConverterStarterTest {

    @Test
    public void testMockConverterStarter() {
        try {
            MockFrlloConverter converter = new MockFrlloConverter(new MockMainController());
            converter.parsePersonDataMap(getMockMap(), true);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

    private LinkedHashMap<String, String> getMockMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("NPERS", "217-478-473 94");
        map.put("ID", "096009217478473676670903514001");
        map.put("RE", "96");
        map.put("FA", "СИСР");
        map.put("IM", "ИОСИФ");
        map.put("OT", "ОЛЬДРИХОВИЧ");
        map.put("RDAT", "1951-09-05");
        map.put("POL", "М");
        map.put("REG_IND", "272313");
        map.put("REG_RE", "Запорожская область");
        map.put("REG_RA", "г.Мелитополь и Мелитопольский район");
        map.put("REG_GOROD", "");
        map.put("REG_PUNKT", "Г МЕЛИТОПОЛЬ");
        map.put("REG_UL", "ПРОСПЕКТ 50-ЛЕТИЯ ПОБЕДЫ");
        map.put("REG_DOM", "36");
        map.put("REG_KOR", "3");
        map.put("REG_KVA", "41");
        map.put("FAKT_RE", "Запорожская область");
        map.put("FAKT_RA", "г.Мелитополь и Мелитопольский район");
        map.put("FAKT_GOROD", "");
        map.put("FAKT_PUNKT", "Г МЕЛИТОПОЛЬ");
        map.put("FAKT_UL", "ПРОСПЕКТ 50-ЛЕТИЯ ПОБЕДЫ");
        map.put("FAKT_DOM", "36/3");
        map.put("FAKT_KOR", "-");
        map.put("FAKT_KVA", "41");
        map.put("KDOK", "ПАСПОРТ РОССИИ");
        map.put("PASS", "60 22");
        map.put("PASN", "875500");
        map.put("PAS_DAT", "2022-08-06");
        map.put("PAS_KEM", "МВД ПО РЕСПУБЛИКЕ КРЫМ");
        map.put("GRAJDAN", "1");
        map.put("VREG", "2023-03-01");
        map.put("L1", "82");
        map.put("L2", "0");
        map.put("NSU1", "0");
        map.put("SROKS", "2023-03-01");
        map.put("CHANGEDATE", "2023-03-10 15:16:07.204");
        map.put("SROKPO", "2025-12-31");
        return map;
    }
}
