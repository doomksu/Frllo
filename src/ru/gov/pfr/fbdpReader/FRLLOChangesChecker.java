package ru.gov.pfr.fbdpReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.personEntities.frlloSources.PersonFRLLOSourceData;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;

/**
 * Класс для проверки изменений в записях контрольной БД по льготникам
 *
 */
public class FRLLOChangesChecker {

    protected final PersonDataSource newPerson;
    protected final ResultSet resultSet;
    private boolean foundInDB = false;
    private boolean hasChanges = false;

    private static final String LOG_CHANGES_CHECKER_RESULT = "LOG_CHANGES_CHECKER_RESULT";
    private static final String _CHANGED_ = "_CHANGED_";
    private static final String _NO_CHANGES_ = "_NO_CHANGES_";

    public FRLLOChangesChecker(PersonDataSource person, ResultSet resultSet) {
        this.newPerson = person;
        this.resultSet = resultSet;
    }

    /**
     * Проверка изменений в переданных ранее записях
     * Сортируем по индексу файла выгрузки
     * @return
     */
    public boolean isPersonChangedOrNew() {
        try {
            if (resultSet == null || resultSet.isClosed()) {
                return true;
            }
            List<PersonFRLLOSourceData> previouslyPersons = new ArrayList<>();
            TreeMap<Integer, PersonFRLLOSourceData> personsInConvertedFiles = new TreeMap<>();
            while (resultSet.next()) {
                foundInDB = true;
                PersonFRLLOSourceData personToControll = new PersonFRLLOSourceData(ConnectionService.resultToMap(resultSet));
                personsInConvertedFiles.put(personToControll.getFileId(), personToControll);
                previouslyPersons.add(personToControll);
            }
            previouslyPersons = previouslyPersons.stream()
                    .sorted(new LoadedPersonsFileIdComparator())
                    .collect(Collectors.toList());

            if (personsInConvertedFiles.isEmpty()) {
                LoggingService.writeLogIfDummy(LOG_CHANGES_CHECKER_RESULT, "person: " + newPerson.getNPERS() + " not in DB", "debug");
                hasChanges = true;
            } else {
                hasChanges = previouslyPersons.get(previouslyPersons.size() - 1).isChanged(newPerson);
            }
            
            LoggingService.writeLogIfDummy(LOG_CHANGES_CHECKER_RESULT,
                    "person: " + newPerson.getNPERS()
                    + (hasChanges ? _CHANGED_ : _NO_CHANGES_), "debug");

            resultSet.getStatement().close();
        } catch (SQLException ex) {
            Logger.getLogger(FRLLOChangesChecker.class
                    .getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return hasChanges;
    }
}
