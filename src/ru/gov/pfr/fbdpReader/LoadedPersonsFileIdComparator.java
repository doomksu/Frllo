package ru.gov.pfr.fbdpReader;

import java.util.Comparator;
import ru.gov.pfr.personEntities.frlloSources.PersonFRLLOSourceData;

class LoadedPersonsFileIdComparator implements Comparator<PersonFRLLOSourceData> {

    public LoadedPersonsFileIdComparator() {
    }

    @Override
    public int compare(PersonFRLLOSourceData o1, PersonFRLLOSourceData o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        Integer o1File = o1.getFileId() == null ? 0 : o1.getFileId();
        Integer o2File = o2.getFileId() == null ? 0 : o2.getFileId();
        return Integer.compare(o1File, o2File);
    }

}
